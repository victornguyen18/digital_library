# Import python lib
import numpy as np
import pandas as pd
import scipy.optimize

# Import Models
from title.models import Book
from transaction.models import Master, Detail


def process_detail_data():
    def cal_point(sample):
        hire_date_length = sample.return_date - sample.hire_date
        due_date_length = sample.due_date - sample.hire_date
        ratio = hire_date_length / due_date_length
        sample['test'] = str(hire_date_length).split()[1]
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

    # Change book to data frame
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
    transaction_df = transaction_df.dropna().reset_index()
    transaction_df = transaction_df.groupby('index').apply(cal_point).reset_index(drop=True)

    # New data frame with user_id, title_id, point
    user_rating = pd.DataFrame()
    user_rating['user_id'] = transaction_df.user_id
    user_rating['title_id'] = transaction_df.title_id
    user_rating['point'] = transaction_df.point
    user_rating_df = user_rating.groupby(['user_id', 'title_id']).mean().reset_index()
    user_rating_df.to_csv(r'recommendation/user_point_title.csv')
    return user_rating_df
