App.TagsShowRoute = Ember.Route.extend({
// TODO: load default tag set
//    model: function() {
//        return this.store.find('tag');
//    }
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