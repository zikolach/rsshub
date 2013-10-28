App.Feed = DS.Model.extend({
    name: DS.attr('string'),
    description: DS.attr('string'),
    updatedAt: DS.attr('isoDate'),
    sources: DS.hasMany('source', { async: true }),
    aggFeedURL: function() {
        return "/feeds/" + this.get("id");
    }.property("id")
});