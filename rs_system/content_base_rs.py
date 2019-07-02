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
from title.models import Title


class RecommendationCB:
    @staticmethod
    def get_top_recommendations_by_title(title_id, top_item=10):
        cosine_sim_title_id = Similarity.objects.filter(source=title_id)
        cosine_sim_title_id = [Similarity.get_similarity_as_list(similarity) for similarity in
                               cosine_sim_title_id]
        # Get the pairwsie similarity scores of all movies with that movie
        sim_scores = list(enumerate(cosine_sim_title_id))
        # Sort the movies based on the similarity scores
        sim_scores = sorted(sim_scores, key=lambda x: x[1], reverse=True)
        # Get the scores of the 10 most similar movies
        sim_scores = sim_scores[1:13]
        title_list = sorted(sim_scores, key=lambda x: x[1], reverse=True)
        title_list = list(dict.fromkeys(title_list))
        title_indices = [i[0] for i in title_list]
        return title_indices[:top_item]

    @staticmethod
    def get_top__recommendations(user_id, top_item=10):
        rating_list = list(Rating.objects.filter(user_id=user_id).order_by('-rating').values())
        title_list = list()
        for rating in rating_list:
            if rating['rating'] > 5:
                title_id = rating['title_id']
                cosine_sim_title_id = Similarity.objects.filter(source=title_id)
                cosine_sim_title_id = [Similarity.get_similarity_as_list(similarity) for similarity in
                                       cosine_sim_title_id]
                # Get the pairwsie similarity scores of all movies with that movie
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


if __name__ == '__main__':
    # for user_index in tqdm(range(ratings_matrix.shape[0])):
    # for user_index in tqdm(range(8, 124)):
    user_index = 8
    print('===========')
    RecommendationCB.get_top__recommendations(user_index)
    print('===========\n')
