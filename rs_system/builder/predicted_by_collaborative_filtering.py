import os
import sys
import django
import logging

sys.path.insert(0, os.path.realpath(''))
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "book_management.settings")
django.setup()
logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s', level=logging.DEBUG)
import rs_system.neighborhood_cf_rs as cf_rs

if __name__ == '__main__':
    res_nb = cf_rs.RecommendationNB()
    res_nb.predict_all_item_and_save()
