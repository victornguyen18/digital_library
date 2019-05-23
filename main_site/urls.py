from django.urls import path, re_path, include
from . import views

urlpatterns = [
    path('recommend', views.recommend, name='recommend'),
    path('search', views.search, name='search')
]
