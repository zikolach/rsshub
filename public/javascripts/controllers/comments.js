App.CommentsRoute = Ember.Route.extend({
    model: function(params) {
        var post = this.modelFor('post');
        return post.get('comments');
    }
});


App.CommentsNewRoute = Ember.Route.extend({
    actions: {
        create: function() {
            var self = this;
            var params = this.controller.getProperties(['comment']);
            var post = this.modelFor('post');
            params['post'] = post;
//            console.log(params);
//            debugger;
            var comment = this.store.createRecord('comment', params);
            comment.save().then(function(comment) {
                post.get('comments').pushObject(comment);
                self.controller.set('comment', '');
                self.transitionTo('comments', post);

            }, function(err) {
                console.log(err);
            });
        },
        cancel: function() {
            var self = this;
            post = this.modelFor('post');
            self.transitionTo('comments', post);
        }
    }

});