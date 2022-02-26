create table User
(
    Email    varchar(50),
    Password varchar(100),
    Username varchar(30),
    Type     varchar(20),
    constraint primary key (email)
);
create table Course
(
    SubjectID       int,
    Term            varchar(20),
    AllowsAnonymous boolean,
    constraint primary key (SubjectID, Term)
);
create table Subject
(
    SubjectID int,
    name      varchar(50),
    constraint primary key (SubjectID)
);
create table Post
(
    PostID      int,
    Text        text,
    Date        date,
    Time        datetime,
    IsAnonymous boolean,
    Author      varchar(50),
    constraint primary key (PostID),
    constraint foreign key (Author) references User (Email)
        on delete set null
        on update cascade
);
create table ThreadPost
(
    PostID int,
    Tag    varchar(15),
    Title  varchar(100),
    constraint primary key (PostID),
    constraint foreign key (PostID) references Post (PostID)
        on delete cascade
        on update cascade
);
create table Reply
(
    PostID    int,
    ReplyToID int,
    constraint primary key (PostID),
    constraint foreign key (ReplyToID) references Post (PostID)
        on delete cascade
        on update cascade
);
create table Folder
(
    FolderID  int,
    Name      varchar(30),
    ParentID  int,
    SubjectID int,
    Term      varchar(20),
    constraint primary key (FolderID),
    constraint foreign key (ParentID) references Folder (FolderID)
        on delete cascade
        on update cascade,
    constraint foreign key (SubjectID, Term) references Course (SubjectID, Term)
        on delete cascade
        on update cascade
);
create table ThreadInFolder
(
    FolderID int,
    PostID   int,
    constraint primary key (FolderID, PostID),
    constraint foreign key (FolderID) references Folder (FolderID)
        on delete cascade
        on update cascade,
    constraint foreign key (PostID) references Post (PostID)
        on delete cascade
        on update cascade
);
create table UserViewedThread
(
    Email  varchar(50),
    Date   date,
    Time   time,
    PostID int,
    constraint primary key (Email, PostID, Time, Date),
    constraint foreign key (Email) references User (Email)
        on delete cascade
        on update cascade,
    constraint foreign key (PostID) references Post (PostID)
        on delete cascade
        on update cascade
);
create table UserLikedPost
(
    Email  varchar(50),
    PostID int,
    Date   date,
    Time   time,
    constraint primary key (Email, PostID, Date, Time),
    constraint foreign key (Email) references User (Email)
        on delete cascade
        on update cascade,
    constraint foreign key (PostID) references Post (PostID)
        on delete cascade
        on update cascade
);
create table AdminInCourse
(
    Email     varchar(50),
    SubjectID int,
    Term      varchar(20),
    constraint primary key (Email, SubjectID),
    constraint foreign key (Email) references User (Email)
        on delete cascade
        on update cascade,
    constraint foreign key (SubjectID, Term) references Course (SubjectID, Term)
        on delete cascade
        on update cascade
);
create table InCourse
(
    Email     varchar(50),
    SubjectID int,
    Term      varchar(20),
    constraint primary key (Email, SubjectID),
    constraint foreign key (Email) references User (Email)
        on delete cascade
        on update cascade,
    constraint foreign key (SubjectID, Term) references Course (SubjectID, Term)
        on delete cascade
        on update cascade
);
create table PostLink
(
    PostID int,
    LinkID int,
    constraint primary key (PostID, LinkID),
    constraint foreign key (PostID) references Post (PostID)
        on delete cascade
        on update cascade,
    constraint foreign key (LinkID) references Post (PostID)
        on delete cascade
        on update cascade
);

show tables
