App.PostsRoute = Ember.Route.extend({
    authRedirectable: true,
     redirect: function() {
        console.log(this);
//         this.transitionTo("posts.search");
     }
 });

App.PostsIndexRoute = Ember.Route.extend({
    authRedirectable: true,
//    redirect: function() {
//        this.transitionTo("posts.search");
//    }
});

App.PostsSearchRoute = Ember.Route.extend({
    authRedirectable: true,
    actions: {
        search: function(criteria) {
            var self = this;
            this.store.findQuery("post", { search: criteria }).then(function(posts) {
                self.controller.set('model', posts);
            });
        }
    }

});

App.PostsSearchController = Ember.ArrayController.extend({
    sortProperties: ['distance','title'],
    sortAscending: true
});

App.PostsNewRoute = Ember.Route.extend({
    authRedirectable: true,
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
    authRedirectable: true,
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
    authRedirectable: true,
    actions: {
        delete: function() {
            var self = this;
            post = this.modelFor('post');
            post.deleteRecord();
            post.save().then(function() {
                self.transitionTo('posts');
            });
        },
        cancel: function() {
            var self = this;
            post = this.modelFor('post');
            self.transitionTo('post', post);
        }
    }
});