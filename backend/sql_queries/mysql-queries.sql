-- drop in dependency-safe order
drop trigger if exists set_expires_at;
drop table if exists sessions;
drop table if exists income;
drop table if exists expenses;
drop table if exists user_account;

-- create user account table
create table if not exists user_account(
account_id int auto_increment primary key,
first_name char(20) not null,
last_name char(20) not null,
username char(20) unique not null,
birthday Date not null,
currency char(10) not null,
password varchar(100) not null,
email char(50) unique not null,
system_date datetime default current_timestamp not null
);

-- create income table
create table if not exists income(
account_id int not null,
transaction_id char(36) primary key,
type char(10) not null,
amount double not null,
source char(50) not null,
description varchar(100) not null,
date date not null,
system_date datetime default current_timestamp not null,
foreign key (account_id) references user_account(account_id)
on update cascade on delete cascade
);

-- create expenses table
create table if not exists expenses(
account_id int not null,
transaction_id char(36) primary key,
type char(10) not null,
amount double not null,
category char(50) not null,
description varchar(100) not null,
date date not null,
system_date datetime default current_timestamp not null,
foreign key (account_id) references user_account(account_id)
on update cascade on delete cascade
);

-- create sessions table
create table if not exists sessions(
session_id char(36) primary key,
account_id int not null,
created_at datetime default current_timestamp not null,
expires_at datetime,
foreign key (account_id) references user_account(account_id)
);

-- trigger for setting the value of expired_at on the sessions table
delimiter //
create trigger set_expires_at
before insert on sessions
for each row
begin
    if new.expires_at is null then
        set new.expires_at = date_add(new.created_at, interval 24 hour);
    end if;
end;
//
delimiter ;

-- Sample selects
select * from user_account;
select * from income;
select * from expenses;
select * from sessions;