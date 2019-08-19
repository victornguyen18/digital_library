import os
import sys
import django
from django.db import connection
from tqdm import tqdm
import logging
import pandas as pd
import numpy as np
import datetime

sys.path.insert(0, os.path.realpath(''))
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "book_management.settings")
django.setup()
logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.DEBUG)
logger = logging.getLogger('User similarity calculator')

# Import Models
from main_site.models import Rating, UserRatingSimilarity, ItemRatingSimilarity
from title.models import Title
from django.contrib.auth.models import User


def specified_rating_indices(u):
    if np.sum(~np.isnan(u)) == 0:
        return None
    else:
        return list(map(tuple, np.where(np.isfinite(u))))


def mean(u):
    if specified_rating_indices(u) is None:
        return np.NaN
    else:
        specified_ratings = u[specified_rating_indices(u)]  # u[np.isfinite(u)]
        m = sum(specified_ratings) / np.shape(specified_ratings)[0]
        return m


def all_user_mean_ratings(rating_matrix):
    return np.array([mean(rating_matrix[u, :]) for u in range(rating_matrix.shape[0])])


def pearson(u, v):
    mean_u = mean(u)
    mean_v = mean(v)
    # If user have not yet rated the book or the item have been rate yet by any user will return NaN value
    if mean_u is None or mean_v is None or specified_rating_indices(u) is None or specified_rating_indices(v) is None:
        return np.NaN

    # Get item which user u is rated
    specified_rating_indices_u = set(specified_rating_indices(u)[0])
    # Get item which user v is rated
    specified_rating_indices_v = set(specified_rating_indices(v)[0])

    # Get list of items which both user u and v is rated
    mutually_specified_ratings_indices = specified_rating_indices_u.intersection(specified_rating_indices_v)
    mutually_specified_ratings_indices = list(mutually_specified_ratings_indices)

    # Get rating of user u and v in  above item list
    u_mutually = u[mutually_specified_ratings_indices]
    v_mutually = v[mutually_specified_ratings_indices]

    # mean-centering rating matrix in user u and v
    centralized_mutually_u = u_mutually - mean_u
    centralized_mutually_v = v_mutually - mean_v

    # calculate pearson similarity
    result = np.sum(np.multiply(centralized_mutually_u, centralized_mutually_v))
    result = result / (
            np.sqrt(np.sum(np.square(centralized_mutually_u))) * np.sqrt(np.sum(np.square(centralized_mutually_v))))

    return result


class CollaborativeFiltering:
    def __init__(self, rating_df=None, save_db=False):
        if rating_df is None:
            self.rating_df = pd.DataFrame(list(Rating.objects.filter(type='calculate').values()))
            logger.info(
                "Calculating similarities ... using {} ratings".format(Rating.objects.filter(type='calculate').count()))
        else:
            self.rating_df = rating_df
        self.rating_matrix, self.item_rating_matrix = self.get_rating_matrix()
        self.mean_centered_ratings_matrix = self.cal_mean_centered_ratings_matrix(self.rating_matrix)
        self.item_mean_centered_ratings_matrix = self.cal_mean_centered_ratings_matrix(self.item_rating_matrix)
        self.user_similarity_matrix = self.cal_similarity_matrix(self.rating_matrix, is_user=True, save_db=save_db)
        self.item_similarity_matrix = self.cal_similarity_matrix(self.item_rating_matrix, is_user=False,
                                                                 save_db=save_db)

    def get_rating_matrix(self):
        # Load data
        logger.info("Load all rating data")
        num_user = User.objects.all().count()
        num_title = Title.objects.all().count()
        # Creating ratings matrix
        logger.info("Creating ratings matrix")
        rating_matrix = np.empty((num_user, num_title))
        rating_matrix[:] = np.nan
        item_rating_matrix = np.empty((num_title, num_user))
        item_rating_matrix[:] = np.nan

        for row in tqdm(self.rating_df.itertuples()):
            rating_matrix[row.user_id - 1, row.title_id - 1] = row.rating
            item_rating_matrix[row.title_id - 1, row.user_id - 1] = row.rating
        return rating_matrix, item_rating_matrix

    @staticmethod
    def cal_mean_centered_ratings_matrix(rating_matrix):
        users_mean_rating = all_user_mean_ratings(rating_matrix)
        mean_centered_ratings_matrix = rating_matrix - np.reshape(users_mean_rating, [-1, 1])
        return mean_centered_ratings_matrix

    @staticmethod
    def cal_similarity_matrix(rating_matrix, is_user, save_db=False):
        if save_db:
            logger.info("TRUNCATE user_rating_similarity TABLE")
            if is_user:
                cur = connection.cursor()
                cur.execute('DELETE FROM `user_rating_similarity`')
            else:
                cur = connection.cursor()
                cur.execute('DELETE FROM `item_rating_similarity`')
        similarity_matrix = []
        for u_index in range(rating_matrix.shape[0]):
            user_ratings = rating_matrix[u_index, :]
            similarity_value = []
            for v_index in range(rating_matrix.shape[0]):
                pearson_similarity = pearson(rating_matrix[v_index, :], user_ratings)
                if not np.isnan(pearson_similarity):
                    if is_user:
                        UserRatingSimilarity.objects.create(
                            source=u_index + 1,
                            target=v_index + 1,
                            similarity=pearson_similarity,
                            created=datetime.datetime.now(),
                        ).save()
                    else:
                        ItemRatingSimilarity.objects.create(
                            source=u_index + 1,
                            target=v_index + 1,
                            similarity=pearson_similarity,
                            created=datetime.datetime.now(),
                        ).save()
                similarity_value.append(pearson_similarity)
            similarity_matrix.append(similarity_value)
        return np.array(similarity_matrix)

    @staticmethod
    def predict(u_index, i_index, rating_matrix, mean_centered_ratings_matrix, user_similarity_matrix):
        # Predict rating
        users_mean_rating = all_user_mean_ratings(rating_matrix)
        similarity_value = user_similarity_matrix[u_index]
        # If user have not yet rated the book or the item have been rate yet by any user will return NaN value
        if specified_rating_indices(rating_matrix[:, i_index]) is None:
            return np.nan
        # Get item which user u_index is rated
        users_rated_item = specified_rating_indices(rating_matrix[:, i_index])[0]
        # Get mean centering rating of item
        mean_ratings_in_item = mean_centered_ratings_matrix[:, i_index]
        # Get rating of item which user u_index is rated
        user_rating = mean_ratings_in_item[np.array(users_rated_item)]
        user_rating_similarity_value = similarity_value[np.array(users_rated_item)]
        # predict rating
        rating_user_base = users_mean_rating[u_index] + np.sum(user_rating * user_rating_similarity_value) / np.sum(
            np.abs(user_rating_similarity_value))
        return rating_user_base

    def predict_all_item(self, save=True):
        if save:
            Rating.objects.filter(type__in=['ub_predicted', 'ib_predicted']).delete()
        rating_predict_df = pd.DataFrame(columns=["user_id", "title_id", "rating", "type"])
        for u_id in tqdm(range(self.rating_matrix.shape[0])):
            for i_id in range(self.rating_matrix.shape[1]):
                # Predict rating base on user-base
                if np.isnan(self.rating_matrix[u_id][i_id]):
                    predicted_rating_user_based = self.predict(u_id, i_id,
                                                               self.rating_matrix,
                                                               self.mean_centered_ratings_matrix,
                                                               self.user_similarity_matrix)
                    if not np.isnan(predicted_rating_user_based):
                        rating_predict_df = rating_predict_df.append(
                            pd.Series({'user_id': u_id + 1,
                                       'title_id': i_id + 1,
                                       'rating': predicted_rating_user_based,
                                       'type': "ub_predicted"}), ignore_index=True)
                        if save:
                            Rating(user_id=u_id + 1,
                                   title_id=i_id + 1,
                                   rating=predicted_rating_user_based,
                                   type="ub_predicted",
                                   rating_timestamp=datetime.datetime.now()).save()
                # Predict rating base on item-base
                if np.isnan(self.item_rating_matrix[i_id][u_id]):
                    predicted_rating_item_based = self.predict(i_id, u_id,
                                                               self.item_rating_matrix,
                                                               self.item_mean_centered_ratings_matrix,
                                                               self.item_similarity_matrix)
                    if not np.isnan(predicted_rating_item_based):
                        rating_predict_df = rating_predict_df.append(
                            pd.Series({'user_id': u_id + 1,
                                       'title_id': i_id + 1,
                                       'rating': predicted_rating_item_based,
                                       'type': "ib_predicted"}), ignore_index=True)
                        if save:
                            Rating(user_id=u_id + 1,
                                   title_id=i_id + 1,
                                   rating=predicted_rating_item_based,
                                   type="ib_predicted",
                                   rating_timestamp=datetime.datetime.now()).save()
        return rating_predict_df

    def predict_top_items_of_user(self, u_index):
        items_list = []
        items = []
        for i_index in range(self.rating_matrix.shape[1]):
            predicted_rating_user_based = 0
            predicted_rating_item_based = 0
            # Predict rating base on user-base
            if np.isnan(self.rating_matrix[u_index][i_index]):
                predicted_rating_user_based = self.predict(u_index, i_index, self.rating_matrix,
                                                           self.mean_centered_ratings_matrix,
                                                           self.user_similarity_matrix)
                if np.isnan(predicted_rating_user_based):
                    predicted_rating_user_based = 0
            # Predict rating base on item-base
            if np.isnan(self.item_rating_matrix[i_index][u_index]):
                predicted_rating_item_based = self.predict(i_index, u_index, self.item_rating_matrix,
                                                           self.item_mean_centered_ratings_matrix,
                                                           self.item_similarity_matrix)
                if np.isnan(predicted_rating_item_based):
                    predicted_rating_item_based = 0
            predicted_rating = (predicted_rating_user_based + predicted_rating_item_based) / 2
            # If rating != 0 add to list
            if predicted_rating != 0:
                items_list.append(i_index)
                items.append(
                    (i_index + 1, predicted_rating, predicted_rating_user_based, predicted_rating_item_based))
        # Sorting base on predited rating
        items = sorted(items, key=lambda tup: tup[1])
        return list(reversed(items))

    def get_list_recommendation(self, user_id):
        logger.info("Predict for user {}".format(user_id))
        rec_list = self.predict_top_items_of_user(user_id - 1)
        # print(len(rec_list))
        # if len(rec_list) == 0:
        #     return []
        # if len(rec_list) - 1 < top_item:
        #     number_of_item = len(rec_list) - 1
        # else:
        #     number_of_item = top_item
        return rec_list


if __name__ == '__main__':
    # for user_index in tqdm(range(1, num_user + 1)):
    #     print(user_index)
    # for user_index in tqdm(range(num_user)):
    user_index = 5
    # logger.info("Print user ratings_matrix")
    # print(ratings_matrix[user_index])
    # predict_top_k_items_of_user(user_index, ratings_matrix, item_ratings_matrix)
    res_nb = CollaborativeFiltering()
    # ratings_matrix, item_ratings_matrix = res_nb.get_rating_matrix()
    print(res_nb.get_list_recommendation(user_index))
