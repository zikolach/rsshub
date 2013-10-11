App.Post = DS.Model.extend({
    title: DS.attr('string'),
    link: DS.attr('string'),
    description: DS.attr('string'),
    pubDate: DS.attr('date'),
    distance: DS.attr('number'),
    tags: DS.hasMany('tag', { async: true })
})