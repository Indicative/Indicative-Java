Java
====

Java Client for Indicative's REST API

This REST client creates a JSON representation of your event and asynchronously posts it to our endpoint.  It has no external dependencies, so you'll never have library conflicts, and it should never slow down or break your app.  You should modify and extend this class to your heart's content.  As a best practice, consider adding a method that takes as a parameter the object representating the user, and adds certain default properties based on that user's characteristics (e.g., gender, age, etc.).

Sample usage: Indicative.event("Your-Api-Key", "Registration").uniqueId("user47").addProperty("name","value").done();

For more details, see our documentation at: http://www.indicative.com/docs/integration.html
