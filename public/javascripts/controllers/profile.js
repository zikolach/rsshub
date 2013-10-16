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
                    function(res) {
                        console.log(res.responseJSON.message);
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
                self.controllerFor('application').alert(res.message);
            });
        }
    }
});

App.ProfileRoute = Ember.Route.extend({
    authRedirectable: true,
//    redirect: function() {
//        this.transitionTo("profile.show");
//    }
});

App.ProfileShowRoute = Ember.Route.extend({
    authRedirectable: true
});

App.ProfileEditRoute = Ember.Route.extend({
    authRedirectable: true,
    model: function() {
        return this.store.find('user', this.auth.get('userId'));
    }
});

//App.ProfileEditController = Ember.ObjectController.extend({});