import os
import sys
import django
import logging
import pandas as pd
import numpy as np
from tqdm import tqdm
from sklearn.metrics import mean_squared_error
from django.db.models import Count

sys.path.insert(0, os.path.realpath(''))
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "book_management.settings")
django.setup()
logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.DEBUG)
from main_site.models import Rating
import rs_system.neighborhood_cf_rs as nb_rs

logger = logging.getLogger('Evaluation')


class EvaluationByPrediction:
    @staticmethod
    def slip_data():
        ratings_count = Rating.objects.all().count()
        test_data_length = round(ratings_count * 10 / 100)
        logger.debug('{} ratings available'.format(ratings_count))
        user_ids = Rating.objects.values('user_id') \
            .annotate(title_count=Count('title_id')) \
            .order_by('-title_count')
        user_ids_df = pd.DataFrame.from_records(user_ids)
        count = 0
        test_data = list()
        for row in tqdm(user_ids_df.itertuples()):
            if row.title_count == 1:
                break
            if row.title_count > 10:
                limit_row = 3
            elif row.title_count > 5:
                limit_row = 2
            else:
                limit_row = 1
            test_data.extend(list(Rating.objects.filter(user_id=row.user_id).order_by('-rating').values()[:limit_row]))
            count += limit_row
            if count == test_data_length:
                break
        test_data_df = pd.DataFrame(test_data)
        test_data_id = list(test_data_df.id)
        train_data_df = pd.DataFrame(list(Rating.objects.exclude(id__in=test_data_id).values()))
        return train_data_df, test_data_df

    def evaluation_in_nb(self):
        train_data_df, test_data_df = self.slip_data()
        res_nb = nb_rs.RecommendationNB(train_data_df)
        # mean_centered_rating_matrix = res_nb.get_mean_centered_ratings_matrix(rating_matrix)
        # item_mean_centered_rating_matrix = res_nb.get_mean_centered_ratings_matrix(item_rating_matrix)
        #
        # user_similarity_matrix = res_nb.get_similarity_matrix(rating_matrix)
        # item_similarity_matrix = res_nb.get_similarity_matrix(item_rating_matrix)

        y_true = []
        y_pred = []
        y_pred_item = []
        print("Start")
        for row in tqdm(test_data_df.itertuples()):
            print(float(row.rating))
            y_true.append(float(row.rating))
            book_list = res_nb.predict_top_items_of_user(row.user_id - 1)
            print('\n', book_list)
            print(row)
            print("=================")
            # predict_result = res_nb.predict(row.user_id - 1, row.title_id - 1, rating_matrix,
            #                                 mean_centered_rating_matrix,
            #                                 user_similarity_matrix)
            # y_pred.append(predict_result)
            # item_predict_result = res_nb.predict(row.title_id - 1, row.user_id - 1, item_rating_matrix,
            #                                      item_mean_centered_rating_matrix,
            #                                      item_similarity_matrix)
            # y_pred_item.append(item_predict_result)
        print("Finished")
        # print(y_true)
        # print(y_pred)
        # print(y_pred_item)


if __name__ == '__main__':
    print('===========')
    EvaluationByPrediction().evaluation_in_nb()
    print('===========\n')
