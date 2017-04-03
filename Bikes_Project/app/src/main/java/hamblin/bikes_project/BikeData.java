package hamblin.bikes_project;

/**
 * See builderpattern example project for how to do builders
 * they are essential when constructing complicated objects and
 * with many optional fields
 */
public class BikeData {
    final String COMPANY;
    final String MODEL;
    final double PRICE;
    final String LOCATION;
    final String DESCRIPTION;
    final String DATE;
    final String PICTURE;
    final String LINK;

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String dialogBlock = "";
        dialogBlock += "Company: " + this.COMPANY + "\n\n";
        dialogBlock += "Model: " + this.MODEL + "\n\n";
        dialogBlock += "Price: " + this.PRICE + "\n\n";
        dialogBlock += "Location: " + this.LOCATION + "\n\n";
        dialogBlock += "Date: " + this.DATE + "\n\n";
        dialogBlock += "Description: " + this.DESCRIPTION + "\n\n";
        dialogBlock += "Link: " + this.LINK + "\n\n";
        return dialogBlock;
    }

    private BikeData(Builder b) {
        //TODO
        this.COMPANY = b.Company;
        this.MODEL = b.Model;
        this.PRICE = b.Price;
        this.LOCATION = (b.Location != null) ? b.Location : "N/A";
        this.DESCRIPTION = (b.Description != null) ? b.Description : "N/A";
        this.DATE = (b.Date != null ) ? b.Date : "N/A";
        this.PICTURE = (b.Picture != null) ? b.Picture : "N/A";
        this.LINK = (b.Link != null) ? b.Link : "N/A";
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
            this.Description = Description;
            return this;
        }

        Builder setLocation(String Location) {
            this.Location = Location;
            return this;
        }

        Builder setDate(String Date) {
            this.Date = Date;
            return this;
        }

        Builder setPicture(String Picture) {
            this.Picture = Picture;
            return this;
        }

        Builder setLink(String Link) {
            this.Link = Link;
            return this;
        }

        // use this to actually construct Bikedata
        // without fear of partial construction
        public BikeData build() {
            return new BikeData(this);
        }
    }
}
