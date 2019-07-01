import os
import argparse
from decimal import Decimal

import logging
import numpy as np
from numpy import random
import pandas as pd
from django.db.models import Count
import rs_system.neighborhood_cf_rs as nb_rs
from evaluate.algorithm_evaluator import PrecisionAtK, MeanAverageError
from sklearn.model_selection import KFold

logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.DEBUG)
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "prs_project.settings")
import django
import time

django.setup()

from main_site.models import Rating


class EvaluationRunner(object):
    def __init__(self, recommence,
                 params=None,
                 logger=logging.getLogger('Evaluation runner')):
        self.recommender = recommence
        self.params = params
        self.logger = logger

    def clean_data(self, ratings, min_ratings=5):
        self.logger.debug("cleaning data only to contain users with at least {} ratings".format(min_ratings))

        original_size = ratings.shape[0]

        user_count = ratings[['user_id', 'title_id']]
        user_count = user_count.groupby('user_id').count()
        user_count = user_count.reset_index()
        user_ids = user_count[user_count['title_id'] > min_ratings]['user_id']

        ratings = ratings[ratings['user_id'].isin(user_ids)]
        new_size = ratings.shape[0]
        self.logger.debug('reduced dataset from {} to {}'.format(original_size, new_size))
        return ratings

    def calculate(self, number_test_users=5):
        ratings_count = Rating.objects.filter(user_id__in=[])
        self.logger.debug('{} ratings available'.format(ratings_count))

        user_ids = Rating.objects.values('user_id') \
            .annotate(title_count=Count('title_id')) \
            .order_by('-title_count')

        test_user_ids = set(user_ids.values_list('user_id', flat=True)[:number_test_users])

        ratings_rows = Rating.objects.filter(user_id__in=test_user_ids).values()

        all_ratings = pd.DataFrame.from_records(ratings_rows)
        return self.calculate_using_ratings_no_cross_validation(all_ratings)

    def calculate_using_ratings_no_cross_validation(self, all_ratings, min_number_of_ratings=5, min_rank=5):
        # ratings = self.clean_data(all_ratings, min_number_of_ratings)
        ratings = all_ratings
        users = ratings.user_id.unique()

        train_data_len = int((len(users) * 70 / 100))
        np.random.seed(42)
        np.random.shuffle(users)
        train_users, test_users = users[:train_data_len], users[train_data_len:]

        test_data, train_data = self.split_data(min_rank,
                                                ratings,
                                                test_users,
                                                train_users)

        self.logger.debug("Test run having {} training rows, and {} test rows".format(len(train_data),
                                                                                      len(test_data)))

        # if self.builder:
        #     if self.params:
        #         self.builder.build(train_data, self.params)
        #         self.logger.debug('setting save_path {}'.format(self.params['save_path']))
        #         self.recommence.set_save_path(self.params['save_path'])
        #     else:
        #         self.builder.build(train_data)
        #
        # self.logger.info("Build is finished")
        #
        # map, ar = PrecisionAtK(self.K, self.recommence).calculate_mean_average_precision(train_data, test_data)
        # mae = 0
        # results = {'map': map, 'ar': ar, 'mae': mae, 'users': len(users)}
        # return results

    @staticmethod
    def split_data(min_rank, ratings, test_users, train_users):
        train = ratings[ratings['user_id'].isin(train_users)]

        test_temp = ratings[ratings['user_id'].isin(test_users)].sort_values('rating_timestamp',
                                                                             ascending=False)

        test = test_temp.groupby('user_id').head(min_rank)

        additional_training_data = test_temp[~test_temp.index.isin(test.index)]

        train = train.append(additional_training_data)

        return test, train


def evaluate_cf_recommence():
    recommender = nb_rs.RecommendationNB()
    er = EvaluationRunner(0, recommender)
    er.calculate()

    return False


def evaluate_cb_recommence():
    return False


if __name__ == '__main__':
    logger = logging.getLogger('Evaluation runner')
    evaluate_cf_recommence()
