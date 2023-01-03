package web.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name="portfolio")
public class Portfolio {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private @Getter @Setter Integer id;
	
	@Column(nullable=false)
    private @Getter @Setter String image;
	
	@Column(nullable=false)
    private @Getter @Setter String name;
	
	@Column(nullable=false)
    private @Getter @Setter String author;
	
	@Column(nullable=false)
    private @Getter @Setter Date date;
	
	@Column(nullable=false)
    private @Getter @Setter String description;
	
	@OneToOne
	@JoinColumn(name="idCategory")
	private @Getter @Setter Category category;
	
	public void reset() {
		this.image=null;
	}
}
