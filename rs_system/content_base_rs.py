import os
import sys
import django
from tqdm import tqdm
import logging
import pandas as pd
import numpy as np

sys.path.insert(0, os.path.realpath(''))
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "book_management.settings")
django.setup()
logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.DEBUG)
logger = logging.getLogger('User similarity calculator')

# Import Models
from main_site.models import Rating
from title.models import Title


def load_all_title():
    db_book_list = Title.objects.all()
    db_book_list = [Title.book_info_as_dict(book) for book in db_book_list]
    db_book_list = pd.DataFrame.from_dict(db_book_list, orient='columns')
    db_book_list['type'] = 'database'
    csv_book_list_df = pd.read_csv('BookData.csv',
                                   names=['id', 'name', 'author', 'pricing', 'pub_year', 'publisher', 'topic',
                                          'type', 'quantility'], header=None)
    csv_book_list_df['faculty'] = ''
    csv_book_list_df['type'] = 'csv'
    csv_book_list_df['id'] = csv_book_list_df['id'] + db_book_list.id.count() - 1
    csv_book_list_df.set_index('id', drop=False, inplace=True)
    book_list_df = pd.concat([db_book_list, csv_book_list_df])
    return book_list_df


class RecommendationCB:

    @staticmethod
    def load_rating(user_id):
        return None


if __name__ == '__main__':
    # for user_index in tqdm(range(ratings_matrix.shape[0])):
    user_index = 23
    # logger.info("Print user ratings_matrix")
    # print(ratings_matrix[user_index])
    # predict_top_k_items_of_user(user_index, ratings_matrix, item_ratings_matrix)
    RecommendationCB.load_rating(user_index)
