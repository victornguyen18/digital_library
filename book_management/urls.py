from django.conf.urls import url, include
from django.contrib import admin
from . import views
from django.contrib.staticfiles.urls import staticfiles_urlpatterns

urlpatterns = [
    # Session
    url(r'^session/create$', views.session_set_json, name='session.create_json'),
    # Title
    url(r'^admin/', include('title.urls')),
    # User
    url(r'^admin/', include('user.urls')),
    url(r'^', include('title.urls_api')),

    # Transaction
    url(r'^admin/', include('transaction.urls')),

    url(r'^admin2/', admin.site.urls),
    url(r'^recommend$', views.recommend, name='recommend'),
    url(r'^log-in$', views.login, name='log-in'),
    url(r'^log-out$', views.logout, name='log-out'),
    url(r'^dashboard$', views.dashboard, name='dashboard'),
    url(r'^user/api/(?P<username>[a-zA_Z0-9]+)$', views.get_user_info),
    # url(r'^dashboard2$', views.get_user_info),
    url(r'^', views.index),
    # API
    # url(r'^songs/(?P<filter_by>[a-zA_Z]+)/$', views.songs, name='songs'),

]

urlpatterns += staticfiles_urlpatterns()
