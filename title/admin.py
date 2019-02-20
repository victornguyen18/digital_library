from django.contrib import admin
from .models import Title, Author, Publisher

# Register your models here.
admin.site.register(Title)
admin.site.register(Author)
admin.site.register(Publisher)
