App.TagsRoute = Ember.Route.extend({
    model: function() {
        return this.store.find('tag');
    }
});

App.TagsController = Ember.ArrayController.extend({
    sortProperties: ['name'],
    sortAscending: true
});