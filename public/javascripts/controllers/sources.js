App.SourcesRoute = Ember.Route.extend({
   model: function() {
        return this.store.find("source");
   }
});

App.SourcesNewRoute = Ember.Route.extend({
    actions: {
        create: function() {
            var self = this;
            var source = this.store.createRecord('source', this.controller.getProperties(['name', 'url']));
            source.save().then(function(source) {
                self.transitionTo('source', source);
            });
        }
    }
});

App.SourceEditRoute = Ember.Route.extend({
    setupController: function(controller) {
        controller.setProperties((this.modelFor('source').getProperties(['name', 'url'])));
    },
    actions: {
        update: function() {
            var self = this;
            source = this.modelFor('source')
            source.setProperties(this.controller.getProperties(['name', 'url']));
            source.save().then(function(source) {
                self.transitionTo('source', source);
            });
        }
    }
});

App.SourceDeleteRoute = Ember.Route.extend({
    actions: {
        delete: function() {
            var self = this;
            source = this.modelFor('source');
            source.deleteRecord();
            source.save().then(function(source) {
                self.transitionTo('sources');
            });
        }
    }
});