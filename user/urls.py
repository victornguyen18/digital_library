from django.urls import path, re_path
from . import views

app_name = 'user'

urlpatterns = [
    # User
    path('user', views.user_index, name='user'),
    path('user/create/', views.user_create, name='user.create'),
]
