from django.db import models


# Create your models here.
class Rating(models.Model):
    class Meta:
        db_table = 'rating'

    user_id = models.IntegerField()
    title_id = models.IntegerField()
    rating = models.DecimalField(decimal_places=2, max_digits=4)
    rating_timestamp = models.DateTimeField()
    type = models.CharField(max_length=10, default='calculate')

    def __str__(self):
        return str(self.id)
        # return "user_id: {}, title_id: {}, rating: {}, type: {}" \
        #     .format(self.user_id, self.title_id, self.rating, self.type)


class Similarity(models.Model):
    created = models.DateField()
    source = models.IntegerField(db_index=True)
    target = models.IntegerField()
    similarity = models.DecimalField(max_digits=8, decimal_places=7)

    class Meta:
        db_table = 'similarity'

    def __str__(self):
        # return [self.similarity]
        return "[]".format(self.similarity)

    def get_similarity_as_list(self):
        return float(self.similarity)

# class Cluster(models.Model):
#     cluster_id = models.IntegerField()
#     user_id = models.IntegerField()
#
#     def __str__(self):
#         return "{} in {}".format(self.user_id, self.cluster_id)
#
#
# class LdaSimilarity(models.Model):
#     created = models.DateField()
#     source = models.IntegerField(db_index=True)
#     target = models.IntegerField()
#     similarity = models.DecimalField(max_digits=8, decimal_places=7)
#
#     class Meta:
#         db_table = 'lda_similarity'
#
#     def __str__(self):
#         return "[({} => {}) sim = {}]".format(self.source,
#                                               self.target,
#                                               self.similarity)
#
#

#
#
# class UserSimilarity(models.Model):
#     created = models.DateField()
#     source = models.IntegerField(db_index=True)
#     target = models.IntegerField()
#     similarity = models.DecimalField(max_digits=8, decimal_places=7)
#
#     class Meta:
#         db_table = 'user_similarity'
#
#     def __str__(self):
#         return "[({} => {}) sim = {}]".format(self.source,
#                                               self.target,
#                                               self.similarity)
#
#
# class SeededRecs(models.Model):
#     created = models.DateTimeField()
#     source = models.IntegerField()
#     target = models.IntegerField()
#     support = models.DecimalField(max_digits=10, decimal_places=8)
#     confidence = models.DecimalField(max_digits=10, decimal_places=8)
#     type = models.CharField(max_length=8)
#
#     class Meta:
#         db_table = 'seeded_recs'
#
#     def __str__(self):
#         return "[({} => {}) s = {}, c= {}]".format(self.source,
#                                                    self.target,
#                                                    self.support,
#                                                    self.confidence)
#
#
# class Recs(models.Model):
#     user = models.IntegerField()
#     item = models.IntegerField()
#     rating = models.FloatField()
#     type = models.CharField(max_length=16)
#
#     class Meta:
#         db_table = 'recs'
#
#     def __str__(self):
#         return "(u,i, t)({}, {}, {})= {}".format(self.user,
#                                                  self.item,
#                                                  self.type,
#                                                  self.rating)
