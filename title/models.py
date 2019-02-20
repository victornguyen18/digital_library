from django.db import models


class Author(models.Model):
    name = models.CharField(max_length=256)

    def __str__(self):
        return self.name


class Publisher(models.Model):
    name = models.CharField(max_length=256)

    def __str__(self):
        return self.name


class Title(models.Model):
    name = models.CharField(max_length=256)
    faculty = models.CharField(max_length=100)
    location = models.CharField(max_length=50, blank=True, null=True)
    year = models.CharField(max_length=5)
    isbn = models.CharField(max_length=20, blank=True, null=True)
    status = models.IntegerField(default=0)
    week_price = models.FloatField(default=0)
    sem_price = models.FloatField(default=0)
    created_time = models.DateTimeField(auto_now_add=True)
    updated_time = models.DateTimeField(auto_now=True)
    ori_quantity = models.IntegerField(blank=True, null=True)
    author = models.ManyToManyField('Author')
    publisher = models.ManyToManyField('Publisher')

    def __str__(self):
        return self.name


class book(models.Model):
    barcode = models.CharField(max_length=20, primary_key=True)
    title = models.ForeignKey(Title)
    status = models.IntegerField(default=0)
    created_time = models.DateTimeField(auto_now_add=True)
    updated_time = models.DateTimeField(auto_now=True)

    def __str__(self):
        return self.barcode

    # 0: deactivate, 1: available, 2: pending, 3: hired, 4: lost, 5: maintained
    def get_status(self):
        if self.status == 0:
            return 'Deactivate'
        elif self.status == 1:
            return 'Available'
        elif self.status == 2:
            return 'Pending'
        elif self.status == 3:
            return 'Hiring'
        elif self.status == 4:
            return 'Lost'
        elif self.status == 5:
            return 'Maintaining'

    def get_status_html(self):
        text = "<span class=\""
        if self.status == 0:
            text = text + "badge badge-dark"
        elif self.status == 1:
            text = text + "badge badge-primary"
        elif self.status == 2:
            text = text + "badge badge-warning"
        elif self.status == 3:
            text = text + "badge badge-success"
        elif self.status == 4:
            text = text + "badge badge-danger"
        elif self.status == 5:
            text = text + "badge badge-dark"
        text = text + "\">" + str(self.get_status()) + "</span>"
        return text
