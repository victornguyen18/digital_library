from django.urls import path, re_path
from . import views

app_name = 'title'

urlpatterns = [
    # Author
    path('author', views.author_index, name='author.index'),
    path('author/create/', views.author_create, name='author.create'),

    # Publisher
    path('publisher', views.publisher_index, name='publisher.index'),
    path('publisher/create/', views.publisher_create, name='publisher.create'),

    # Title
    path('title', views.title_index, name='title.index'),
    path('title/create/', views.title_create, name='title.create'),
]
