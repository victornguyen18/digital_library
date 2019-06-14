from django.db import models


class Rating(models.Model):
    user_id = models.CharField(max_length=16)
    title_id = models.CharField(max_length=16)
    rating = models.DecimalField(decimal_places=2, max_digits=4)
    rating_timestamp = models.DateTimeField()
    type = models.CharField(max_length=10, default='calculate')

    def __str__(self):
        return "user_id: {}, title_id: {}, rating: {}, type: {}" \
            .format(self.user_id, self.title_id, self.rating, self.type)


class Cluster(models.Model):
    cluster_id = models.IntegerField()
    user_id = models.IntegerField()

    def __str__(self):
        return "{} in {}".format(self.user_id, self.cluster_id)
