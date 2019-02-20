from django.conf.urls import url
from . import views

app_name = 'user'

urlpatterns = [
    # User
    url(r'^user$', views.user_index, name='user'),
    url(r'^user/create/$', views.user_create, name='user.create'),
]
