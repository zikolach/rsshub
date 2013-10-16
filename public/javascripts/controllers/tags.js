App.TagsIndexRoute = Ember.Route.extend({
    authRedirectable: true,
    redirect: function() {
//        this.transitionTo("tags.show");
    }
});

App.TagsRoute = Ember.Route.extend({
    authRedirectable: true
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
    authRedirectable: true,
    model: function(params) {
        return this.store.find('tag', params.tag_id)
    }
});