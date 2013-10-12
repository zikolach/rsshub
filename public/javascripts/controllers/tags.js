App.TagsIndexRoute = Ember.Route.extend({
    redirect: function() {
        this.transitionTo("tags.show");
    }
});

App.TagsShowController = Ember.ArrayController.extend({
    sortProperties: ['posts.length', 'name'],
    sortAscending: false,
    actions: {
        search: function() {
            this.set('model', this.store.find('tag', { search: this.get('criteria') }))
        }
    }
});

App.TagRoute = Ember.Route.extend({
    model: function(params) {
        return this.store.find('tag', params.tag_id)
    }
});