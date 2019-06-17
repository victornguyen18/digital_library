import os, django, logging
import pandas as pd
import numpy as np
from datetime import datetime

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "book_management.settings")

# Setup env
django.setup()
from recommendation.models import Rating

logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.DEBUG)
logger = logging.getLogger('User similarity calculator')


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

    if mean_u is None or mean_v is None or specified_rating_indices(u) is None or specified_rating_indices(v) is None:
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


def get_user_similarity_value_for(u_index, ratings_matrix):
    user_ratings = ratings_matrix[u_index, :]
    similarity_value = np.array([pearson(ratings_matrix[i, :], user_ratings) for i in range(ratings_matrix.shape[0])])
    return similarity_value


def get_user_similarity_matrix(ratings_matrix):
    similarity_matrix = []
    for u_index in range(ratings_matrix.shape[0]):
        similarity_value = get_user_similarity_value_for(u_index, ratings_matrix)
        similarity_matrix.append(similarity_value)
    return np.array(similarity_matrix)


def predict(u_index, i_index, user_similarity_matrix, ratings_matrix, mean_centered_ratings_matrix):
    # k là số lượng người dùng giống với người dùng cần dự đoán
    # ta có thể tùy chọn giá trị k này
    users_mean_rating = all_user_mean_ratings(ratings_matrix)

    similarity_value = user_similarity_matrix[u_index]
    sorted_users_similar = np.argsort(similarity_value)
    sorted_users_similar = np.flip(sorted_users_similar, axis=0)
    if specified_rating_indices(ratings_matrix[:, i_index]) is None:
        return np.nan
    users_rated_item = specified_rating_indices(ratings_matrix[:, i_index])[0]

    ranked_similar_user_rated_item = [u for u in sorted_users_similar if u in users_rated_item]

    top_k_similar_user = np.array(ranked_similar_user_rated_item)

    ratings_in_item = mean_centered_ratings_matrix[:, i_index]
    top_k_ratings = ratings_in_item[top_k_similar_user]

    top_k_similarity_value = similarity_value[top_k_similar_user]

    r_hat = users_mean_rating[u_index] + np.sum(top_k_ratings * top_k_similarity_value) / np.sum(
        np.abs(top_k_similarity_value))
    return r_hat


def predict_top_k_items_of_user(u_index, user_similarity_matrix, ratings_matrix, mean_centered_ratings_matrix):
    items = []
    for i_index in range(ratings_matrix.shape[1]):
        if np.isnan(ratings_matrix[u_index][i_index]):
            rating = predict(u_index, i_index, user_similarity_matrix, ratings_matrix, mean_centered_ratings_matrix)
            if not np.isnan(rating):
                items.append((i_index, rating))
    items = sorted(items, key=lambda tup: tup[1])
    return list(reversed(items))


class UserSimilarityMatrixBuilder:

    def __init__(self):
        logger.info("Load all rating data")
        self.rating_df = pd.DataFrame(list(Rating.objects.all().values()))
        self.num_user = self.rating_df.user_id.max()
        self.num_title = self.rating_df.title_id.max()
        logger.debug("Calculating similarities ... using {} ratings".format(len(self.rating_df)))
        start_time = datetime.now()
        logger.debug("Creating ratings matrix")
        ratings_matrix = np.empty((self.num_user + 1, self.num_title + 1))
        ratings_matrix[:] = np.nan
        for row in self.rating_df.itertuples():
            ratings_matrix[row.user_id, row.title_id] = row.rating
        self.ratings_matrix = ratings_matrix

    def build(self, save=True):
        user_similarity_matrix = get_user_similarity_matrix(self.ratings_matrix)
        return user_similarity_matrix

    def get_ratings_matrix(self):
        return self.ratings_matrix


def main():
    logger.info("Calculation of item similarity")

    # all_ratings = load_all_ratings()
    user_similarity = UserSimilarityMatrixBuilder()
    user_similarity_matrix = user_similarity.build(save=True)
    ratings_matrix = user_similarity.get_ratings_matrix()
    mean_centered_ratings_matrix = get_mean_centered_ratings_matrix(ratings_matrix)
    for user_index in range(ratings_matrix.shape[0]):
        print(predict_top_k_items_of_user(user_index, user_similarity_matrix, ratings_matrix, mean_centered_ratings_matrix))


if __name__ == '__main__':
    main()
