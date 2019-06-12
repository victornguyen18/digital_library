# Import python lib
import numpy as np
import pandas as pd
import scipy.optimize

# Import Models
from title.models import Book
from transaction.models import Master, Detail

import calculate_point as cp


def my_recommend():
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

    df = pd.read_csv('recommendation/user_point_title.csv')
    mynu = df.user_id.max()
    my_nm = df.title_id.max()
    my_nf = 5
    Y = np.zeros((my_nm, mynu))
    for row in df.itertuples():
        Y[row[2] - 1, row[1] - 1] = row[3]
    R = np.zeros((my_nm, mynu))
    for i in range(Y.shape[0]):
        for j in range(Y.shape[1]):
            if Y[i][j] != 0:
                R[i][j] = 1

    Ynorm, Ymean = normalize_ratings(Y, R)
    X = np.random.rand(my_nm, my_nf)
    Theta = np.random.rand(mynu, my_nf)
    myflat = flatten_params(X, Theta)
    mylambda = 12.2
    result = scipy.optimize.fmin_cg(cofi_cost_func, x0=myflat, fprime=cofi_grad,
                                    args=(Y, R, mynu, my_nm, my_nf, mylambda),
                                    maxiter=40, disp=True, full_output=True)
    resX, resTheta = reshape_params(result[0], my_nm, mynu, my_nf)
    prediction_matrix = resX.dot(resTheta.T)
    return prediction_matrix, Ymean
    # return user_rating_df
