from django.urls import path, re_path, include
from . import views

urlpatterns = [
    # path('recommend', views.recommend, name='recommend'),
    path('search', views.search, name='search'),
    re_path(r'^book/detail/(?P<title_id>[0-9-]+)$', views.book_detail),
    path('profile', views.profile, name='profile'),
    path('api/get-popular-book', views.get_popular_book, name='get_popular_book'),
    # path('api/get_user_based_rs', views.get_user_based_rs, name='get_user_based_rs'),
    path('api/get_recommendation_cf', views.get_recommendation_cf, name='get_recommendation_cf'),
    path('api/get_recommendation_cb', views.get_recommendation_cb, name='get_recommendation_cb'),
    re_path('api/get_recommendation_cb_with_title', views.get_recommendation_cb_with_title,
            name='get_recommendation_cb_with_title'),
]
