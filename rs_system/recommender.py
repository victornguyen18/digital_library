import os
import sys
import django
import pandas as pd

sys.path.insert(0, os.path.realpath(''))
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "book_management.settings")
django.setup()

# Import Models
from main_site.models import Rating, Similarity
from title.models import Title


def get_popular_book(top_item=12):
    try:
        df = pd.DataFrame(list(Rating.objects.all().values()))
        df['user_id'] = pd.to_numeric(df['user_id'], errors='coerce')
        df['title_id'] = pd.to_numeric(df['title_id'], errors='coerce')
        df['rating'] = pd.to_numeric(df['rating'], errors='coerce')
        popular_rs_df = (df.groupby(by=['title_id'])
        ['rating'].mean().round(3).reset_index().rename(columns={'rating': 'total_rating'})
        [['title_id', 'total_rating']])
        popular_rs_df = popular_rs_df.sort_values(by=['total_rating'], ascending=False).reset_index(drop=True)
        return list(popular_rs_df.title_id)[:top_item]
    except Exception as e:
        print("Something wrong:", str(e))
        return list()


def get_top_recs_using_content_based(title_id, top_item=12):
    cosine_sim_title_id = Similarity.objects.filter(source=title_id)
    cosine_sim_title_id = [Similarity.get_similarity_as_list(similarity) for similarity in
                           cosine_sim_title_id]
    # Get the pairwise similarity scores of all movies with that movie
    sim_scores = list(enumerate(cosine_sim_title_id))
    # Sort the movies based on the similarity scores
    sim_scores = sorted(sim_scores, key=lambda x: x[1], reverse=True)
    # Get the scores of the 10 most similar movies
    sim_scores = sim_scores[1:13]
    content_based_rs_list = sorted(sim_scores, key=lambda x: x[1], reverse=True)
    content_based_rs_list = list(dict.fromkeys(content_based_rs_list))
    content_based_rs_indices = [i[0] for i in content_based_rs_list]
    return content_based_rs_indices[:top_item]


def get_top_recs_using_content_based_with_user_rating(user_index, top_item=12):
    rating_list = list(Rating.objects.filter(user_id=user_index, type='calculate').order_by('-rating').values())
    content_based_rs_list = list()
    for rating in rating_list[:5]:
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
        sim_scores = sim_scores[1:top_item + 1]
        content_based_rs_list.extend(sim_scores)
    content_based_rs_df = pd.DataFrame(content_based_rs_list, columns=['title_id', 'similarity'])
    content_based_rs_df = content_based_rs_df.groupby(['title_id']).mean().reset_index()
    content_based_rs_df = content_based_rs_df.sort_values(by=['similarity'], ascending=False).reset_index()
    # Get the title indices
    return list(content_based_rs_df["title_id"])[:top_item]


def process_in_collaborative_filtering(sample):
    total_rating = sample.rating.sum()
    return pd.Series(dict(title_id=sample['title_id'].values[0],
                          rating=total_rating / 2))


def get_top_recs_using_collaborative_filtering(user_index, top_item=12, rating_df=None):
    if rating_df is None:
        predicted_rating_df = pd.DataFrame(list(
            Rating.objects.filter(user_id=user_index, type__in=['ib_predicted', 'ub_predicted']).order_by(
                '-rating').values()))
    else:
        predicted_rating_df = rating_df[rating_df.user_id == user_index]
    cf_rating_df = predicted_rating_df.groupby(by=['title_id']).apply(process_in_collaborative_filtering).reset_index(
        drop=True)
    cf_rating_df = cf_rating_df.sort_values(by=['rating'], ascending=False).reset_index(drop=True)
    return list(cf_rating_df["title_id"])[:top_item]


def process_rating_in_hybrid(sample):
    cf_rating = sum(sample[sample.type == 'collaborative_filtering'].rating) * 0.7
    cb_rating = sum(sample[sample.type == 'content_based'].rating) * 0.3
    return pd.Series(dict(title_id=int(sample['title_id'].values[0]),
                          rating=cf_rating + cb_rating))


def get_top_recs_using_hybrid(user_index, top_item=12, rating_df=None):
    # Get collaborative filtering rs
    if rating_df is None:
        predicted_rating_df = pd.DataFrame(list(
            Rating.objects.filter(
                user_id=user_index, type__in=['ib_predicted', 'ub_predicted']).order_by(
                '-rating').values()))
    else:
        predicted_rating_df = rating_df[rating_df.user_id == user_index]
    cf_rating_df = predicted_rating_df. \
        groupby(by=['title_id']). \
        apply(process_in_collaborative_filtering). \
        reset_index(drop=True)
    cf_rating_df['type'] = "collaborative_filtering"
    # Get content-based rs
    content_based_rs_list = get_top_recs_using_content_based(user_index)
    # Get rating of title
    title_info_in_cb = Title.objects.filter(pk__in=content_based_rs_list)
    title_info_in_cb_df = pd.DataFrame(list(title_info_in_cb.values()))
    # Merge 2 set rs into hybrid rs
    hybrid_rs_list_df = pd.DataFrame()
    hybrid_rs_list_df['title_id'] = title_info_in_cb_df['id']
    hybrid_rs_list_df['rating'] = title_info_in_cb_df['rating']
    hybrid_rs_list_df['type'] = "content_based"
    hybrid_rs_list_df = hybrid_rs_list_df.append(cf_rating_df, ignore_index=True, )
    hybrid_rs_list_df['rating'] = pd.to_numeric(hybrid_rs_list_df['rating'], errors='coerce')
    # Calculate rating = content-based * 0.3 + collaborating filtering rs
    result_hybrid_rs_list_df = hybrid_rs_list_df.groupby(by=['title_id']) \
        .apply(process_rating_in_hybrid) \
        .reset_index(drop=True)
    result_hybrid_rs_list_df['title_id'] = pd.to_numeric(result_hybrid_rs_list_df['title_id'],
                                                         downcast='integer',
                                                         errors='coerce')
    # Sort rating
    result_hybrid_rs_list_df = result_hybrid_rs_list_df.sort_values(by=['rating'], ascending=False).reset_index(
        drop=True)
    return list(result_hybrid_rs_list_df["title_id"])[:top_item]


if __name__ == '__main__':
    user_id = 5
    # rs_list = get_top_recs_using_hybrid(user_index=user_id)
    rs_list = get_top_recs_using_content_based(user_id)
    print(rs_list)
    # res_nb = cl_calculator.CollaborativeFiltering()
    # print(get_top_recs_using_collaborative_filtering(5, rating_df=res_nb.predict_all_item()))
