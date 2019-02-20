from django.db import models
from django.contrib.auth.models import Permission, User
from title.models import book
from datetime import datetime


# Create your models here.
class detail(models.Model):
    user = models.ForeignKey(User)
    address = models.CharField(max_length=255, blank=True, null=True)
    phone = models.CharField(max_length=255, blank=True, null=True)