from django.conf.urls import url
from . import views

app_name = 'title'

urlpatterns = [
    # Author
    url(r'^author$', views.author_index, name='author.index'),
    url(r'^author/create/$', views.author_create, name='author.create'),

    # Publisher
    url(r'^publisher$', views.publisher_index, name='publisher.index'),
    url(r'^publisher/create/$', views.publisher_create, name='publisher.create'),

    # Title
    url(r'^title$', views.title_index, name='title.index'),
    url(r'^title/create/$', views.title_create, name='title.create'),
]
