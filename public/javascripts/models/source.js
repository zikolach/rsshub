App.Source = DS.Model.extend({
    name: DS.attr('string'),
    url: DS.attr('string'),
    fetchDate: DS.attr('isoDate'),
    feeds: DS.hasMany('feed', { async: true }),
    commentsFeedURL: function() {
        return "/comments/" + this.get("id");
    }.property("id")
});