package org.blim.whist;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.IndexColumn;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

@Entity
public class Game {

	private Long id;
	private Date creationDate;	
	private List<Round> rounds = Lists.newArrayList();	
	private List<String> players = Lists.newArrayList();
	private int[] roundSequence;
	
	@Id
	@GeneratedValue
	public Long getId() { return this.id; }
    public void setId(Long id) { this.id = id; }
    	
	@Temporal(TemporalType.TIMESTAMP)
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	@CollectionOfElements
	@IndexColumn(name = "sortkey")
	public List<String> getPlayers() {
		return players;
	}

	public void setPlayers(List<String> players) {
		this.players = players;
	}

	@OneToMany(cascade = CascadeType.ALL)
	public List<Round> getRounds() {
		return rounds;
	}

	public void setRounds(List<Round> rounds) {
		this.rounds = rounds;
	}
	
	public void setRoundSequence(int[] roundSequence) {
		this.roundSequence = roundSequence;
	}
	
	@CollectionOfElements
	@IndexColumn(name = "sortkey")
	public int[] getRoundSequence() {
		return roundSequence;
	}
	public int getPlayerIndex(String name) {
		return players.indexOf(name);
	}
	
	@Transient
	public Round getCurrentRound() {
		return Iterables.getLast(rounds);
	}

}
