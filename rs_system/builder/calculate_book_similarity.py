import os
import sys
import django
from django.db import connection
from tqdm import tqdm
import logging
import pandas as pd
from datetime import datetime
import re

sys.path.insert(0, os.path.realpath(''))
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "book_management.settings")
django.setup()
logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.DEBUG)
logger = logging.getLogger('User similarity calculator')

# Import Models
from title.models import Title
from main_site.models import Similarity

from sklearn.metrics.pairwise import cosine_similarity
from sklearn.feature_extraction.text import CountVectorizer

features = ['author', 'author', 'faculty', 'faculty', 'publisher']


def clean_data(x):
    if isinstance(x, str):
        x = str.lower(x.replace(" ", ""))
        x = x.replace("unknown", " ")
        return x.replace(",", " ")
    else:
        return ''


def create_bag_of_words(x):
    # Remove special character
    name_temp = re.sub(r"[^a-zA-Z]+", ' ', x['name'])
    name_temp = name_temp.replace(" st", "")
    name_temp = name_temp.replace(" nd", "")
    name_temp = name_temp.replace(" rd", "")
    name_temp = name_temp.replace(" th", "")
    name_temp = name_temp.replace(" ed", "")
    temp = ''
    temp += str.lower(name_temp)
    temp += str.lower(name_temp)
    temp += str.lower(name_temp)
    for feature in features:
        temp += x[feature + '_clean'] + ' '
    return temp


class CalculateItemSimilarity(object):

    def calculate(self, run_force=False):
        if run_force:
            logger.info("Calculate item similarity")
            logger.info("Load all item")
            book_list_df = self.load_data()
            logger.info("Save rating into database")
            logger.info("Clean data")
            # Apply clean_data function to your features.
            for feature in features:
                book_list_df[feature + '_clean'] = book_list_df[feature].apply(clean_data)
            # Create meta bag of words
            book_list_df['bag_of_words'] = book_list_df.apply(create_bag_of_words, axis=1)
            # Convert word to vector and calculate similarity
            # instantiating and generating the count matrix
            count = CountVectorizer(stop_words='english')
            count_matrix = count.fit_transform(book_list_df['bag_of_words'])
            # generating the cosine similarity matrix base on cosine_sim
            cosine_sim_matrix = cosine_similarity(count_matrix, count_matrix)
            logger.info("Save similarity into database")
            self.save_similarity(cosine_sim_matrix)
        else:
            logger.debug("Already calculating")

    @staticmethod
    def save_similarity(cosine_sim_matrix):
        logger.info("TRUNCATE SIMILARITY TABLE")
        cur = connection.cursor()
        cur.execute('TRUNCATE TABLE `similarity`')
        for i in tqdm(range(cosine_sim_matrix.shape[0])):
            for j in range(cosine_sim_matrix.shape[1]):
                Similarity(
                    source=i + 1,
                    target=j + 1,
                    similarity=cosine_sim_matrix[i][j],
                    created=datetime.now(),
                ).save()
        logger.info("Finished saving")

    @staticmethod
    def load_data():
        db_book_list = Title.objects.all().order_by('id')
        db_book_list = [Title.book_info_as_dict(book) for book in db_book_list]
        db_book_list = pd.DataFrame.from_dict(db_book_list, orient='columns')
        db_book_list['type'] = 'database'
        return db_book_list


if __name__ == '__main__':
    obj_call = CalculateItemSimilarity()
    obj_call.calculate(run_force=True)
