import os
import sys
import django
import logging
import pandas as pd
import numpy as np
from datetime import datetime
from tqdm import tqdm
from django.db.models import Count
from sklearn.metrics import mean_squared_error

sys.path.insert(0, "/Users/victornguyen/Sites/07.book_management/")
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "book_management.settings")
django.setup()
logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.DEBUG)
from title.models import Book
from transaction.models import Master, Detail
import rs_system.recommender as recommendation

logger = logging.getLogger('Evaluation')


def cal_point(sample):
    hire_date_length = abs(sample.return_date - sample.hire_date)
    due_date_length = abs(sample.due_date - sample.hire_date)
    ratio = hire_date_length / due_date_length
    sample['hire_date_length'] = hire_date_length
    sample['due_date_length'] = due_date_length
    sample['ratio_hire_date'] = round(ratio, 3)
    if (ratio > 2).bool():
        sample['point'] = 4.50
    elif (ratio > 1).bool():
        ratio_overdue = abs(1 - ratio)
        sample['point'] = 3 + (7 - (7 * ratio_overdue))
    else:
        sample['test'] = abs((1 - ratio) / (ratio - 1))
        sample['point'] = 3 + (7 * ratio)
    sample['point'] = round(sample['point'], 2)
    return sample


class EvaluationByTransaction:
    @staticmethod
    def slip_data(transaction_df):
        transaction_count = Detail.objects.all().count()
        test_data_count = round(transaction_count * 10 / 100)
        transaction_count_df = transaction_df[['title_id', 'user_id']].groupby('user_id').agg('count')
        transaction_count_df = transaction_count_df.sort_values(by=['title_id'], ascending=False)
        transaction_count_df.rename(columns={'title_id': 'count_title_id'},
                                    inplace=True)
        total_transaction_df = pd.merge(transaction_df, transaction_count_df, left_on="user_id",
                                        right_on="user_id")
        total_transaction_df = total_transaction_df.sort_values(by=['count_title_id', 'user_id', 'hire_date'],
                                                                ascending=False).reset_index(drop=True)
        test_user_tile = list()
        count = 0
        current_row = 0
        remove_row = []
        while True:
            row = total_transaction_df.iloc[current_row]
            if row.count_title_id == 1:
                break
            if row.count_title_id > 10:
                limit_row = 3
            elif row.count_title_id > 5:
                limit_row = 2
            else:
                limit_row = 1
            for i in range(limit_row):
                remove_row.append(current_row + i)
            test_user_tile.append(dict([('user_id', row.user_id), (
                'title_id', list(total_transaction_df.iloc[current_row:current_row + limit_row]['title_id']))]))
            current_row = current_row + row.count_title_id
            count += limit_row
            if count == test_data_count:
                break
        train_transaction_df = total_transaction_df.drop(remove_row).reset_index(drop=True)
        transaction_df = train_transaction_df.groupby('id_y').apply(cal_point).reset_index(drop=True)
        # New data frame with user_id, title_id, point
        train_data_df = pd.DataFrame()
        train_data_df['user_id'] = transaction_df.user_id
        train_data_df['title_id'] = transaction_df.title_id
        train_data_df['rating'] = transaction_df.point
        train_data_df = transaction_df.groupby(['user_id', 'title_id']).mean().round(3).reset_index()
        return train_data_df, test_user_tile

    @staticmethod
    def load_transaction_data():
        book_df = pd.DataFrame(list(Book.objects.all().values()))
        # Change transaction_master_db to data frame
        transaction_master_df = pd.DataFrame(list(Master.objects.all().values()))
        # Change name column
        transaction_master_df.rename(columns={'date': 'hire_date'},
                                     inplace=True)
        # Change transaction_detail_db to data frame
        transaction_detail_df = pd.DataFrame(list(Detail.objects.all().values()))
        # Join two data frame to one
        transaction_df = pd.merge(transaction_master_df, transaction_detail_df, left_on='id', right_on='transaction_id')
        transaction_df = pd.merge(transaction_df, book_df[['barcode', 'title_id']], left_on="book_id",
                                  right_on="barcode",
                                  how="left")
        transaction_df = transaction_df.drop(['id_x', 'transaction_id'], axis=1)
        transaction_df['return_date'] = transaction_df['return_date'].fillna(datetime.now())
        return transaction_df

    def evaluation_in_cf(self):
        transaction_df = self.load_transaction_data()
        train_data_df, test_user_tile = self.slip_data(transaction_df)
        print(len(test_user_tile))
        error_list = []
        for item in test_user_tile:
            book_id_list = []
            rec_list = recommendation.get_top_recs_using_collaborative_filtering(item['user_id'], 12)
            print(rec_list)
            for i in rec_list:
                book_id_list.append(int(i[0]) + 1)
            if len(book_id_list) != 0:
                print('user', item['user_id'])
                print(item['title_id'])
                print('predict:', book_id_list)
                print(list(set(item['title_id']) & set(rec_list)))
                error = len(list(set(item['title_id']) & set(book_id_list))) / len(book_id_list)
                print('ERROR', error)
                error_list.append(error)
        print(error_list)
        return np.sum(error_list) / len(error_list)

    def evaluation_in_cb(self):
        transaction_df = self.load_transaction_data()
        train_data_df, test_user_tile = self.slip_data(transaction_df)
        print(len(test_user_tile))
        res_cb = recommendation.get_top_recs_using_content_based()
        error_list = []
        for item in test_user_tile:
            rec_list = res_cb.get_top__recommendations(item['user_id'], 10)
            if len(rec_list) != 0:
                print('user', item['user_id'])
                print(item['title_id'])
                print('predict:', rec_list)
                print(list(set(item['title_id']) & set(rec_list)))
                error = len(list(set(item['title_id']) & set(rec_list))) / len(rec_list)
                print('ERROR', error)
                error_list.append(error)
        print(error_list)
        return np.sum(error_list) / len(error_list)


if __name__ == '__main__':
    print('===========')
    evaluation = EvaluationByTransaction()
    # print("Evaluation of CF")
    # print(evaluation.evaluation_in_cf())
    # print('===========\n')
    print("Evaluation of CB")
    print(evaluation.evaluation_in_cb())
    print('===========\n')
    print('===========\n')
