# Import python lib
import numpy as np
import pandas as pd
import scipy.optimize

# Import Models
from main_site.models import Rating


class PopularRS:
    @staticmethod
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


if __name__ == '__main__':
    # for user_index in tqdm(range(ratings_matrix.shape[0])):
    user_index = 23
    # logger.info("Print user ratings_matrix")
    # print(ratings_matrix[user_index])
    # predict_top_k_items_of_user(user_index, ratings_matrix, item_ratings_matrix)
    PopularRS().get_popular_book()
