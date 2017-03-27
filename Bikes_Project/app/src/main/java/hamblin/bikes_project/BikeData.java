package hamblin.bikes_project;

/**
 * See builderpattern example project for how to do builders
 * they are essential when constructing complicated objects and
 * with many optional fields
 */
public class BikeData {
    public static final int COMPANY = 0;
    public static final int MODEL = 1;
    public static final int PRICE = 2;
    public static final int LOCATION = 3;

    //TODO make all BikeData fields final

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        // TODO figure out how to print all bikedata out for dialogs
        return "TODO";
    }

    private BikeData(Builder b) {
        //TODO
    }

    /**
     * @author lynn builder pattern, see page 11 Effective Java UserData mydata
     *         = new
     *         UserData.Builder(first,last).addProject(proj1).addProject(proj2
     *         ).build()
     */
    public static class Builder {
        final String Company;
        final String Model;
        final Double Price;
        String Description;
        String Location;
        String Date;
        String Picture;
        String Link;

        // Model and price required
        Builder(String Company, String Model, Double Price) {
            this.Company = Company;
            this.Model = Model;
            this.Price = Price;
        }

        // the following are setters
        // notice it returns this bulder
        // makes it suitable for chaining
        Builder setDescription(String Description) {
            //TODO manage this
            return this;
        }

        Builder setLocation(String Location) {

            return this;
        }

        Builder setDate(String Date) {

            return this;
        }

        Builder setPicture(String Picture) {

            return this;
        }

        Builder setLink(String Link) {

            return this;
        }

        // use this to actually construct Bikedata
        // without fear of partial construction
        public BikeData build() {
            return new BikeData(this);
        }
    }
}
