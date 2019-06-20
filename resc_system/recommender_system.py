# Import python lib
import numpy as np
import pandas as pd
import scipy.optimize

# Import Models
from recommendation.models import Rating


def recommend_cf():
    def normalize_ratings(my_y, my_r):
        # The mean is only counting movies that were rated
        y_mean = np.sum(my_y, axis=1) / np.sum(my_r, axis=1)
        y_mean = y_mean.reshape((y_mean.shape[0], 1))
        return my_y - y_mean, y_mean

    def flatten_params(my_x, my_theta):
        return np.concatenate((my_x.flatten(), my_theta.flatten()))

    def reshape_params(flattened_x_and_theta, my_nm, my_nu, my_nf):
        assert flattened_x_and_theta.shape[0] == int(my_nm * my_nf + my_nu * my_nf)
        re_x = flattened_x_and_theta[:int(my_nm * my_nf)].reshape((my_nm, my_nf))
        re_theta = flattened_x_and_theta[int(my_nm * my_nf):].reshape((my_nu, my_nf))
        return re_x, re_theta

    def cofi_cost_func(my_params, my_y, my_r, my_nu, my_nm, my_nf, my_lambda=0.):
        my_x, my_theta = reshape_params(my_params, my_nm, my_nu, my_nf)
        term1 = my_x.dot(my_theta.T)
        term1 = np.multiply(term1, my_r)
        cost = 0.5 * np.sum(np.square(term1 - my_y))
        # for regularization
        cost += (my_lambda / 2.) * np.sum(np.square(my_theta))
        cost += (my_lambda / 2.) * np.sum(np.square(my_x))
        return cost

    def cofi_grad(my_params, myY, myR, my_nu, my_nm, my_nf, my_lambda=0.):
        my_x, my_theta = reshape_params(my_params, my_nm, my_nu, my_nf)
        term1 = my_x.dot(my_theta.T)
        term1 = np.multiply(term1, myR)
        term1 -= myY
        x_grad = term1.dot(my_theta)
        Thetagrad = term1.T.dot(my_x)
        # Adding Regularization
        x_grad += my_lambda * my_x
        Thetagrad += my_lambda * my_theta
        return flatten_params(x_grad, Thetagrad)

    try:
        df = pd.DataFrame(list(Rating.objects.all().values()))
        df['user_id'] = pd.to_numeric(df['user_id'], errors='coerce')
        df['title_id'] = pd.to_numeric(df['title_id'], errors='coerce')
        df['rating'] = pd.to_numeric(df['rating'], errors='coerce')
        my_nu = df.user_id.max()
        my_nm = df.title_id.max()
        my_nf = 5
        Y = np.zeros((my_nm, my_nu))
        for row in df.itertuples():
            Y[row.title_id - 1, row.user_id - 1] = row.rating
        R = np.zeros((my_nm, my_nu))
        for i in range(Y.shape[0]):
            for j in range(Y.shape[1]):
                if Y[i][j] != 0:
                    R[i][j] = 1

        Ynorm, Ymean = normalize_ratings(Y, R)
        X = np.random.rand(my_nm, my_nf)
        Theta = np.random.rand(my_nu, my_nf)
        myflat = flatten_params(X, Theta)
        mylambda = 12.2
        result = scipy.optimize.fmin_cg(cofi_cost_func, x0=myflat, fprime=cofi_grad,
                                        args=(Y, R, my_nu, my_nm, my_nf, mylambda),
                                        maxiter=40, disp=True, full_output=True)
        resX, resTheta = reshape_params(result[0], my_nm, my_nu, my_nf)
        prediction_matrix = resX.dot(resTheta.T)
        return prediction_matrix, Ymean
    except Exception as e:
        print("Something wrong:", str(e))
        return None, None


def user_based_rs():
    try:
        # df = pd.read_csv('resc_system/user_point_title.csv')
        df = pd.DataFrame(list(Rating.objects.all().values()))
        df['user_id'] = pd.to_numeric(df['user_id'], errors='coerce')
        df['title_id'] = pd.to_numeric(df['title_id'], errors='coerce')
        df['rating'] = pd.to_numeric(df['rating'], errors='coerce')
        popular_book_df = (df.groupby(by=['title_id'])
        ['point'].mean().round(3).reset_index().rename(columns={'point': 'total_point'})
        [['title_id', 'total_point']])
        popular_book_df = popular_book_df.sort_values(by=['total_point'], ascending=False).reset_index(drop=True)
        print(popular_book_df)
        return list(popular_book_df.title_id)
    except Exception as e:
        print("Something wrong:", str(e))
        return list()


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