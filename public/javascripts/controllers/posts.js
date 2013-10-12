App.PostsRoute = Ember.Route.extend({
//   model: function() {
//        return this.store.findQuery("post");
//   }
//    redirect: function() {
//        this.transitionTo("posts.search");
//    }
});

App.PostsSearchController = Ember.ArrayController.extend({
    sortProperties: ['distance','title'],
    sortAscending: true,
    actions: {
        search: function(criteria) {
            this.set('model', this.store.findQuery("post", { search: criteria }));
        }
    }
});

App.PostsNewRoute = Ember.Route.extend({
    actions: {
        create: function() {
            var self = this;
            var post = this.store.createRecord('post', this.controller.getProperties(['title', 'link', 'description', 'pubDate']));
            post.save().then(function(post) {
                self.transitionTo('post', post);
            });
        }
    }
});

App.PostEditRoute = Ember.Route.extend({
    setupController: function(controller) {
        controller.setProperties((this.modelFor('post').getProperties(['title', 'link', 'description', 'pubDate'])));
    },
    actions: {
        update: function() {
            var self = this;
            post = this.modelFor('post')
            post.setProperties(this.controller.getProperties(['title', 'link', 'description', 'pubDate']));
            post.save().then(function(post) {
                self.transitionTo('post', post);
            });
        }
    }
});

App.PostDeleteRoute = Ember.Route.extend({
    actions: {
        delete: function() {
            var self = this;
            post = this.modelFor('post');
            post.deleteRecord();
            post.save().then(function() {
                self.transitionTo('posts');
            });
        }
    }
});