import os, time, django

# Import python lib
import pandas as pd
from stat import *  # ST_SIZE etc
import datetime as dt

# Import Models
from title.models import Book
from transaction.models import Master, Detail

from recommendation.models import Rating, Cluster

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "book_management.settings")


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


class CalculatePointAllUser(object):

    def calculate(self):
        print("Calculate user point in title_id")
        user_ratings_df = self.load_data()
        print("Saving")
        self.save_pointing(user_ratings_df)

    @staticmethod
    def save_pointing(user_ratings_df):
        print("Delete all old point")
        Rating.objects.filter(type='calculate').delete()
        print("Saving new point")
        for row in user_ratings_df.itertuples():
            Rating(
                user_id=row[1],
                title_id=row[2],
                rating=row[3],
                rating_timestamp=dt.datetime.now(),
            ).save()

    @staticmethod
    def load_data():
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
        user_rating_df = user_rating.groupby(['user_id', 'title_id']).mean().round(3).reset_index()
        return user_rating_df


if __name__ == '__main__':
    print("Calculating user point for title_id...")

    cluster = CalculatePointAllUser()
    cluster.calculate()
