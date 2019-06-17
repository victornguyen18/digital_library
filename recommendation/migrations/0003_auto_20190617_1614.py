# Generated by Django 2.2.1 on 2019-06-17 16:14

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('recommendation', '0002_ldasimilarity_seededrecs_similarity'),
    ]

    operations = [
        migrations.CreateModel(
            name='Recs',
            fields=[
                ('id', models.AutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('user', models.IntegerField()),
                ('item', models.IntegerField()),
                ('rating', models.FloatField()),
                ('type', models.CharField(max_length=16)),
            ],
            options={
                'db_table': 'recs',
            },
        ),
        migrations.AlterField(
            model_name='ldasimilarity',
            name='source',
            field=models.IntegerField(db_index=True),
        ),
        migrations.AlterField(
            model_name='ldasimilarity',
            name='target',
            field=models.IntegerField(),
        ),
        migrations.AlterField(
            model_name='rating',
            name='title_id',
            field=models.IntegerField(),
        ),
        migrations.AlterField(
            model_name='rating',
            name='user_id',
            field=models.IntegerField(),
        ),
        migrations.AlterField(
            model_name='seededrecs',
            name='source',
            field=models.IntegerField(),
        ),
        migrations.AlterField(
            model_name='seededrecs',
            name='target',
            field=models.IntegerField(),
        ),
        migrations.AlterField(
            model_name='similarity',
            name='source',
            field=models.IntegerField(db_index=True),
        ),
        migrations.AlterField(
            model_name='similarity',
            name='target',
            field=models.IntegerField(),
        ),
    ]
