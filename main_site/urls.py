from django.urls import path, re_path, include
from . import views

urlpatterns = [
    # path('recommend', views.recommend, name='recommend'),
    path('search', views.search, name='search'),
    path('api/get-popular-book', views.get_popular_book, name='get_popular_book'),
    path('api/get_user_based_rs', views.get_user_based_rs, name='get_user_based_rs'),
    path('api/get_recommendation', views.get_recommendation, name='get_recommendation'),
]
