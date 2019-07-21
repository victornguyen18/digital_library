import os
import sys
import django
from django.db import connection
from tqdm import tqdm
import logging
import pandas as pd
from datetime import datetime

sys.path.insert(0, os.path.realpath(''))
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "book_management.settings")
django.setup()
logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.DEBUG)
logger = logging.getLogger('User similarity calculator')

# Import Models
from title.models import Book
from transaction.models import Master, Detail
from main_site.models import Rating


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


class CalculatePointAllUser(object):

    def calculate(self, run_force=False):
        rating_first = Rating.objects.filter(type='calculate').order_by('rating_timestamp').first()
        if rating_first is None:
            diff_time = 3
        else:
            time_rating_first = str(rating_first.rating_timestamp)
            time_rating_first = time_rating_first[0:time_rating_first.find('.')]
            check_time = datetime.strptime(time_rating_first, '%Y-%m-%d %H:%M:%S')
            diff_time = (datetime.now() - check_time).days

        if diff_time > 2 or run_force:
            logger.info("Calculate user point in title_id")
            user_ratings_df = self.load_data()
            logger.info("Save rating into database")
            self.save_pointing(user_ratings_df)
        else:
            logger.debug("Calculate rating before" + str(diff_time) + "days")

    @staticmethod
    def save_pointing(user_ratings_df):
        logger.info("TRUNCATE RATING TABLE")
        cur = connection.cursor()
        cur.execute('TRUNCATE TABLE `rating`')
        Rating.objects.filter(type='calculate').delete()
        for row in tqdm(user_ratings_df.itertuples()):
            Rating(
                user_id=row[1],
                title_id=row[2],
                rating=row[3],
                rating_timestamp=datetime.now(),
            ).save()
        logger.info("Finished saving " + str(len(user_ratings_df)) + " records")

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
        # transaction_df = transaction_df.dropna().reset_index()
        # transaction_df = transaction_df.groupby('index').apply(cal_point).reset_index(drop=True)
        transaction_df['return_date'] = transaction_df['return_date'].fillna(datetime.now())
        transaction_df = transaction_df.groupby('id_y').apply(cal_point).reset_index(drop=True)

        # New data frame with user_id, title_id, point
        user_rating = pd.DataFrame()
        user_rating['user_id'] = transaction_df.user_id
        user_rating['title_id'] = transaction_df.title_id
        user_rating['point'] = transaction_df.point
        user_rating_df = user_rating.groupby(['user_id', 'title_id']).mean().round(3).reset_index()
        return user_rating_df


if __name__ == '__main__':
    logger.info("Calculate user point in title_id")
    obj_call = CalculatePointAllUser()
    obj_call.calculate(run_force=True)
