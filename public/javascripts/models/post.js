App.Post = DS.Model.extend({
    title: DS.attr('string'),
    link: DS.attr('string'),
    description: DS.attr('string'),
    pubDate: DS.attr('isoDate'),
    distance: DS.attr('number'),
    tags: DS.hasMany('tag', { async: true }),
    comments: DS.hasMany('comment', { async: true }),
    userId: DS.attr('number')
})