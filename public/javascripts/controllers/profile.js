App.LoginRoute = Ember.Route.extend({
    actions: {
        login: function() {
            var self = this;
            var data = this.controller.getProperties(['name', 'password']);
            var rememberMe = this.controller.get('rememberMe');
            this.auth.signIn({data : data}).then(function(res) {
                if (!rememberMe)
                    self.auth.get('module.rememberable').forget();
                self.controllerFor('application').alert(res.message);
            }, function(err) {
                self.controllerFor('application').alert(err.message);
            });
        }
    }
});


App.RegisterRoute = Ember.Route.extend({
    actions: {
        register: function() {
            var self = this;
            var data = this.controller.getProperties(['name', 'password', 'confirmation']);
            if (data.password !== data.confirmation)
                self.controllerFor('application').alert("Password and confirmation must be equal");
            else {
                delete data['confirmation'];
                $.postJSON("/api/v1/register", data).then(
                    function(res) {
                        self.controllerFor('application').alert(res.message);
                        self.transitionTo("login");
                    },
                    function(err) {
                        self.controllerFor('application').alert(err.responseJSON.message);
                    }
                );
            }
        }
    }
});

App.LogoutRoute = Ember.Route.extend({
    authRedirectable: true,
    actions: {
        logout: function() {
            var self = this;
            var auth = this.get('auth');
            auth.signOut().then(function(res) {
                self.store.unloadAll('feed');
                self.store.unloadAll('source');
                self.store.unloadAll('post');
                self.store.unloadAll('tag');
                self.store.unloadAll('user');
                self.controllerFor('application').alert(res.message);
            }, function(err) {
                self.controllerFor('application').alert(err.responseJSON.message);
            });
        }
    }
});

App.ProfileRoute = Ember.Route.extend({
    authRedirectable: true,
    model: function() {
        return this.store.find('user', this.auth.get('userId'));
    }
});

App.ProfileShowRoute = Ember.Route.extend({
    authRedirectable: true
});

App.ProfileEditRoute = Ember.Route.extend({
    authRedirectable: true,
    setupController: function(controller) {
        controller.setProperties(this.modelFor('profile').getProperties(['name']))
    },
    actions: {
        update: function() {
            var self = this;
            var user = this.modelFor('profile');
            var passwords = this.controller.getProperties(['password', 'confirmation']);
            if (passwords.password || passwords.confirmation)
                if (passwords.password !== passwords.confirmation) {
                    this.controllerFor('application').alert('Password and confirmation must be equal');
                    return;
                } else {
                    user.set('password', passwords.password);
                    user.save().then(function() {
                        self.controller.set('password', '')
                        self.controller.set('confirmation', '')
                        self.transitionTo("profile.show");
                    });
                }
        }
    }
});
