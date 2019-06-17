import os
import pandas as pd
import logging
from sklearn.metrics.pairwise import cosine_similarity
from scipy.sparse import coo_matrix
from datetime import datetime

os.environ.setdefault("DJANGO_SETTINGS_MODULE", "book_management.settings")

import django

django.setup()

from recommendation.models import Similarity, Rating

logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.DEBUG)
logger = logging.getLogger('Item simialarity calculator')


def normalize(x):
    x = x.astype(float)
    x_sum = x.sum()
    x_num = x.astype(bool).sum()
    x_mean = x_sum / x_num

    if x_num == 1 or x.std() == 0:
        return 0.0
    return (x - x_mean) / (x.max() - x.min())


def load_all_ratings(min_ratings=1):
    columns = ['user_id', 'title_id', 'rating', 'type']
    ratings_data = Rating.objects.all().values(*columns)
    return ratings_data


class ItemSimilarityMatrixBuilder(object):

    def __init__(self, min_overlap=15, min_sim=0.2):
        self.min_overlap = min_overlap
        self.min_sim = min_sim

    def build(self, ratings, save=True):
        print(len(ratings))
        logger.debug("Calculating similarities ... using {} ratings".format(len(ratings)))
        start_time = datetime.now()

        logger.debug("Creating ratings matrix")
        ratings['rating'] = ratings['rating'].astype(float)
        # test = pd.DataFrame(ratings.groupby('user_id')['rating'])
        # Get rating of each user in each row and cal mean
        # ratings['avg'] = ratings.groupby('user_id')['rating'].transform(lambda x: normalize(x))
        #
        # ratings['avg'] = ratings['avg'].astype(float)
        ratings['user_id'] = ratings['user_id'].astype('category')
        ratings['title_id'] = ratings['title_id'].astype('category')

        coo = coo_matrix((ratings['rating'].astype(float),
                          (ratings['title_id'].cat.codes.copy(),
                           ratings['user_id'].cat.codes.copy())))
        logger.debug("Calculating overlaps between the items")

        start_time = datetime.now()
        cor = cosine_similarity(coo, dense_output=False)
        # cor = rp.corr(method='pearson', min_periods=self.min_overlap)
        # cor = (cosine(rp.T))

        cor = cor.multiply(cor > self.min_sim)
        test = pd.DataFrame(ratings['title_id'].cat.categories)
        title = dict(enumerate(ratings['title_id'].cat.categories))
        print(title)
        logger.debug('Correlation is finished, done in {} seconds'.format(datetime.now() - start_time))
        if save:
            start_time = datetime.now()
            logger.debug('save starting')
            self._save_with_django(cor, title)

            logger.debug('save finished, done in {} seconds'.format(datetime.now() - start_time))

        return cor, title

    def _save_with_django(self, sm, index, created=datetime.now()):
        start_time = datetime.now()
        Similarity.objects.all().delete()
        logger.info(f'truncating table in {datetime.now() - start_time} seconds')
        sims = []
        no_saved = 0
        start_time = datetime.now()
        coo = coo_matrix(sm)
        csr = coo.tocsr()

        logger.debug(f'instantiation of coo_matrix in {datetime.now() - start_time} seconds')
        logger.debug(f'{coo.count_nonzero()} similarities to save')
        xs, ys = coo.nonzero()
        for x, y in zip(xs, ys):

            if x == y:
                continue
            sim = csr[x, y]

            if sim < self.min_sim:
                continue

            # if len(sims) == 500000:
            #     Similarity.objects.bulk_create(sims)
            #     sims = []
            #     logger.debug(f"{no_saved} saved in {datetime.now() - start_time}")

            # new_similarity = Similarity(
            #     source=index[x],
            #     target=index[y],
            #     created=created,
            #     similarity=sim
            # )

            Similarity(
                source=index[x],
                target=index[y],
                created=created,
                similarity=sim
            ).bulk_create(sims)
            # no_saved += 1
            # sims.append(new_similarity)

        # Similarity.objects.bulk_create(sims)
        logger.info('{} Similarity items saved, done in {} seconds'.format(no_saved, datetime.now() - start_time))


def main():
    logger.info("Calculation of item similarity")
    all_ratings = load_all_ratings()
    ItemSimilarityMatrixBuilder(min_overlap=20, min_sim=0.0).build(all_ratings)


if __name__ == '__main__':
    main()
