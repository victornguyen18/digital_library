from django.urls import path, re_path, include
from django.contrib import admin
from django.views.generic import RedirectView

from . import views
from django.contrib.staticfiles.urls import staticfiles_urlpatterns

urlpatterns = [
    path('favicon.ico', RedirectView.as_view(url='/static/images/favicon.ico')),
    path('', views.homepage, name='homepage'),
    path('', include('main_site.urls')),
    path('index', views.index),
    # Session
    path('session/create', views.session_set_json, name='session.create_json'),
    # Title
    path('admin/', include('title.urls')),
    # User
    path('admin/', include('user.urls')),
    path('', include('title.urls_api')),
    # Transaction
    path('admin/', include('transaction.urls')),
    path('admin2/', admin.site.urls),
    path('log-in', views.login, name='log-in'),
    path('log-out', views.logout, name='log-out'),
    path('dashboard', views.dashboard, name='dashboard'),
    re_path('user/api/(?P<username>[a-zA_Z0-9]+)$', views.get_user_info),
    # API

]

urlpatterns += staticfiles_urlpatterns()
