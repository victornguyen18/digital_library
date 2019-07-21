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
from main_site.models import Rating, Similarity


def get_popular_book():
    try:
        df = pd.DataFrame(list(Rating.objects.all().values()))
        df['user_id'] = pd.to_numeric(df['user_id'], errors='coerce')
        df['title_id'] = pd.to_numeric(df['title_id'], errors='coerce')
        df['rating'] = pd.to_numeric(df['rating'], errors='coerce')
        popular_book_df = (df.groupby(by=['title_id'])
        ['rating'].mean().round(3).reset_index().rename(columns={'rating': 'total_rating'})
        [['title_id', 'total_rating']])
        popular_book_df = popular_book_df.sort_values(by=['total_rating'], ascending=False).reset_index(drop=True)
        print(popular_book_df)
        return list(popular_book_df.title_id)
    except Exception as e:
        print("Something wrong:", str(e))
        return list()


def get_top_recs_using_content_based_in_title(title_id, top_item=12):
    cosine_sim_title_id = Similarity.objects.filter(source=title_id)
    cosine_sim_title_id = [Similarity.get_similarity_as_list(similarity) for similarity in
                           cosine_sim_title_id]
    # Get the pairwise similarity scores of all movies with that movie
    sim_scores = list(enumerate(cosine_sim_title_id))
    # Sort the movies based on the similarity scores
    sim_scores = sorted(sim_scores, key=lambda x: x[1], reverse=True)
    # Get the scores of the 10 most similar movies
    sim_scores = sim_scores[1:13]
    title_list = sorted(sim_scores, key=lambda x: x[1], reverse=True)
    title_list = list(dict.fromkeys(title_list))
    title_indices = [i[0] for i in title_list]
    return title_indices[:top_item]


def get_top_recs_using_content_based(user_index, top_item=12):
    rating_list = list(Rating.objects.filter(user_id=user_index).order_by('-rating').values())
    title_list = list()
    for rating in rating_list:
        # if rating['rating'] > 5:
        title_id = rating['title_id']
        cosine_sim_title_id = Similarity.objects.filter(source=title_id)
        cosine_sim_title_id = [Similarity.get_similarity_as_list(similarity) for similarity in
                               cosine_sim_title_id]
        # Get the pairwise similarity scores of all movies with that movie
        sim_scores = list(enumerate(cosine_sim_title_id))
        # Sort the movies based on the similarity scores
        sim_scores = sorted(sim_scores, key=lambda x: x[1], reverse=True)
        # Get the scores of the 10 most similar movies
        sim_scores = sim_scores[1:11]
        title_list.extend(sim_scores)
    title_list = sorted(title_list, key=lambda x: x[1], reverse=True)
    title_list = list(dict.fromkeys(title_list))
    # Get the title indices
    title_indices = [i[0] for i in title_list]
    return title_indices[:top_item]


def process(sample):
    total_rating = sample.rating.sum()
    return pd.Series(dict(title_id=sample['title_id'].values[0],
                          rating=total_rating / 2))


def get_top_recs_using_collaborative_filtering(user_index, top_item=12):
    rating_df = pd.DataFrame(list(
        Rating.objects.filter(user_id=user_index, type__in=['ib_predicted', 'ub_predicted']).order_by(
            '-rating').values()))
    cf_rating_df = rating_df.groupby(by=['title_id']).apply(process).reset_index(drop=True)
    cf_rating_df = cf_rating_df.sort_values(by=['rating'], ascending=False).reset_index(drop=True)
    return list(cf_rating_df["title_id"])[:top_item]


if __name__ == '__main__':
    user_id = 5
    print(get_top_recs_using_collaborative_filtering(5))
