# Import python lib
import numpy as np
import pandas as pd
import scipy.optimize

# Import Models
from django.contrib.auth.models import User
from title.models import Title, Book
from transaction.models import Master, Detail


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

    def process_date(sample):
        date_obj = sample.return_date - sample.hire_date
        # year = int(date_obj[:4])
        # month = int(date_obj[4:6])
        # day = int(date_obj[6:8])
        # sample['year'] = year
        # sample['month'] = month
        sample['hire_date_length_temp'] = date_obj
        sample['due_date_length_temp'] = sample.due_date - sample.hire_date
        return sample

    # Change book to data frame
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
    transaction_df = pd.merge(transaction_df, book_df[['barcode', 'title_id']], left_on="book_id", right_on="barcode",
                              how="left")
    transaction_df = transaction_df.drop(['id_x', 'transaction_id'], axis=1)
    transaction_df = transaction_df.dropna().reset_index(drop=True)
    # transaction_df = transaction_df.groupby('index').apply(process_date)
    transaction_df.to_csv(r'recommendation/transaction.csv')
    print(transaction_df)
    print(transaction_df.columns.values)
    return transaction_df

    # mynu = df.user_id.unique().shape[0]
    # mynm = df.movie_id.unique().shape[0]
    # mynf = 10
    # Y = np.zeros((mynm, mynu))
    # for row in df.itertuples():
    #     Y[row[2] - 1, row[4] - 1] = row[3]
    # R = np.zeros((mynm, mynu))
    # for i in range(Y.shape[0]):
    #     for j in range(Y.shape[1]):
    #         if Y[i][j] != 0:
    #             R[i][j] = 1
    #
    # Ynorm, Ymean = normalize_ratings(Y, R)
    # X = np.random.rand(mynm, mynf)
    # Theta = np.random.rand(mynu, mynf)
    # myflat = flatten_params(X, Theta)
    # mylambda = 12.2
    # result = scipy.optimize.fmin_cg(cofi_cost_func, x0=myflat, fprime=cofi_grad,
    #                                 args=(Y, R, mynu, mynm, mynf, mylambda),
    #                                 maxiter=40, disp=True, full_output=True)
    # resX, resTheta = reshape_params(result[0], mynm, mynu, mynf)
    # prediction_matrix = resX.dot(resTheta.T)
    # return prediction_matrix, Ymean
