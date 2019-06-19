import os
import django
import logging
import sys
import pandas as pd
import numpy as np
from tqdm import tqdm

sys.path.insert(0, '/Users/victornguyen/Sites/07.book_management')
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "book_management.settings")

# Setup env
django.setup()
from recommendation.models import Rating

logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.DEBUG)
logger = logging.getLogger('User similarity calculator')


def get_list_recommendation(user_index):
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

    def all_user_mean_ratings(ratings_matrix):
        return np.array([mean(ratings_matrix[u, :]) for u in range(ratings_matrix.shape[0])])

    def get_mean_centered_ratings_matrix(ratings_matrix):
        users_mean_rating = all_user_mean_ratings(ratings_matrix)
        mean_centered_ratings_matrix = ratings_matrix - np.reshape(users_mean_rating, [-1, 1])
        return mean_centered_ratings_matrix

    def pearson(u, v):
        mean_u = mean(u)
        mean_v = mean(v)

        if mean_u is None or mean_v is None or specified_rating_indices(u) is None or specified_rating_indices(
                v) is None:
            return np.NaN

        specified_rating_indices_u = set(specified_rating_indices(u)[0])
        specified_rating_indices_v = set(specified_rating_indices(v)[0])

        mutually_specified_ratings_indices = specified_rating_indices_u.intersection(specified_rating_indices_v)
        mutually_specified_ratings_indices = list(mutually_specified_ratings_indices)

        u_mutually = u[mutually_specified_ratings_indices]
        v_mutually = v[mutually_specified_ratings_indices]

        centralized_mutually_u = u_mutually - mean_u
        centralized_mutually_v = v_mutually - mean_v

        result = np.sum(np.multiply(centralized_mutually_u, centralized_mutually_v))
        result = result / (
                np.sqrt(np.sum(np.square(centralized_mutually_u))) * np.sqrt(np.sum(np.square(centralized_mutually_v))))

        return result

    def get_similarity_value_for(u_index, ratings_matrix):
        user_ratings = ratings_matrix[u_index, :]
        similarity_value = np.array(
            [pearson(ratings_matrix[i, :], user_ratings) for i in range(ratings_matrix.shape[0])])
        return similarity_value

    def get_similarity_matrix(ratings_matrix):
        similarity_matrix = []
        for u_index in range(ratings_matrix.shape[0]):
            similarity_value = get_similarity_value_for(u_index, ratings_matrix)
            similarity_matrix.append(similarity_value)
        return np.array(similarity_matrix)

    def get_item_similarity_value_for(i_index, ratings_matrix):
        title_ratings = ratings_matrix[:, i_index]
        similarity_value = np.array(
            [pearson(ratings_matrix[:, i], title_ratings) for i in range(ratings_matrix.shape[1])])
        return similarity_value

    def get_item_similarity_matrix(ratings_matrix):
        similarity_matrix = []
        for i_index in range(ratings_matrix.shape[1]):
            similarity_value = get_item_similarity_value_for(i_index, ratings_matrix)
            similarity_matrix.append(similarity_value)
        return np.array(similarity_matrix)

    def predict(u_index, i_index, ratings_matrix, mean_centered_ratings_matrix, user_similarity_matrix):
        # k là số lượng người dùng giống với người dùng cần dự đoán
        # ta có thể tùy chọn giá trị k này
        # Predict by user - based item
        users_mean_rating = all_user_mean_ratings(ratings_matrix)
        similarity_value = user_similarity_matrix[u_index]
        # Sort user similar
        # sorted_users_similar = np.argsort(similarity_value)
        # sorted_users_similar = np.flip(sorted_users_similar, axis=0)
        if specified_rating_indices(ratings_matrix[:, i_index]) is None:
            return np.nan

        # Get list of user which rate i_index title
        users_rated_item = specified_rating_indices(ratings_matrix[:, i_index])[0]
        # Rank user having similar
        # ranked_similar_user_rated_item = [u for u in sorted_users_similar if u in users_rated_item]
        # top_k_similar_user = np.array(ranked_similar_user_rated_item)
        # Get mean centering rating of item
        mean_ratings_in_item = mean_centered_ratings_matrix[:, i_index]
        user_rating = mean_ratings_in_item[np.array(users_rated_item)]
        user_rating_similarity_value = similarity_value[np.array(users_rated_item)]
        # top_k_ratings = ratings_in_item[np.array(users_rated_item)]
        # top_k_similarity_value = similarity_value[np.array(users_rated_item)]

        rating_user_base = users_mean_rating[u_index] + np.sum(user_rating * user_rating_similarity_value) / np.sum(
            np.abs(user_rating_similarity_value))
        return rating_user_base

    def predict_top_k_items_of_user(u_index, ratings_matrix, item_ratings_matrix=None):
        # logger.debug("Calculating  mean-centering ratings matrix of user and title")
        mean_centered_ratings_matrix = get_mean_centered_ratings_matrix(ratings_matrix)
        item_mean_centered_ratings_matrix = get_mean_centered_ratings_matrix(item_ratings_matrix)

        # logger.debug("Calculating similarities user and similarities title")
        user_similarity_matrix = get_similarity_matrix(ratings_matrix)
        item_similarity_matrix = get_similarity_matrix(item_ratings_matrix)

        items = []
        for i_index in tqdm(range(ratings_matrix.shape[1])):
            predicted_rating_user_based = 0
            predicted_rating_item_based = 0
            if np.isnan(ratings_matrix[u_index][i_index]):
                predicted_rating_user_based = predict(u_index, i_index, ratings_matrix,
                                                      mean_centered_ratings_matrix, user_similarity_matrix)
                if np.isnan(predicted_rating_user_based):
                    predicted_rating_user_based = 0
            if np.isnan(item_ratings_matrix[i_index][u_index]):
                predicted_rating_item_based = predict(i_index, u_index, item_ratings_matrix,
                                                      item_mean_centered_ratings_matrix, item_similarity_matrix)
                if np.isnan(predicted_rating_item_based):
                    predicted_rating_item_based = 0
            predicted_rating = predicted_rating_user_based + predicted_rating_item_based
            if predicted_rating != 0:
                items.append((i_index, predicted_rating, predicted_rating_user_based, predicted_rating_item_based))
        items = sorted(items, key=lambda tup: tup[1])
        return list(reversed(items))

    # Load data
    logger.info("Load all rating data")
    rating_df = pd.DataFrame(list(Rating.objects.all().values()))
    num_user = rating_df.user_id.max()
    num_title = rating_df.title_id.max()
    # Creating ratings matrix
    logger.debug("Creating ratings matrix")
    ratings_matrix = np.empty((num_user, num_title))
    ratings_matrix[:] = np.nan
    item_ratings_matrix = np.empty((num_title, num_user))
    item_ratings_matrix[:] = np.nan

    for row in tqdm(rating_df.itertuples()):
        ratings_matrix[row.user_id - 1, row.title_id - 1] = row.rating
        item_ratings_matrix[row.title_id - 1, row.user_id - 1] = row.rating

    logger.info("Predict for user")
    # for user_index in tqdm(range(ratings_matrix.shape[0])):
    # user_index = 122
    # logger.info("Print user ratings_matrix")
    # print(ratings_matrix[user_index])
    # predict_top_k_items_of_user(user_index, ratings_matrix, item_ratings_matrix)
    print('\n', user_index + 1, ':', predict_top_k_items_of_user(user_index, ratings_matrix, item_ratings_matrix))

#
# def main():
#     # for user_index in tqdm(range(ratings_matrix.shape[0])):
#     user_index = 122
#     get_list_recommendation(user_index)
#
#
# if __name__ == '__main__':
#     main()
